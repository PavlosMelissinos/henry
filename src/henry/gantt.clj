(ns henry.gantt
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]

            [applied-science.darkstar :as darkstar]
            [dali.batik :as batik]
            [oz.core :as oz]
            [taoensso.timbre :as log]

            [henry.graph :as graph]
            [henry.utils :as utils]))

(def default-cfg (utils/load-edn (io/resource "defaults.edn")))

(defn- duration->end [{:keys [start duration] :as m}]
  (if duration (assoc m :end (+ start duration)) m))

(defn vega-lite-spec [{:keys [tasks dependencies styles] :as cfg}]
  (log/info "Building vega lite spec...")
  (let [tasks (->> (graph/build tasks dependencies)
                   (graph/assign-task-beginnings)
                   (map #(assoc % :label (or (:label %) (:id %))))
                   (map #(utils/style-node % styles))
                   (map duration->end))]
    (-> (merge default-cfg cfg)
        (assoc-in [:data :values] tasks)
        (dissoc :tasks :dependencies))))

(defn html [spec]
  (oz/html spec))

(defn json [spec]
  (json/write-str spec))

(defn svg [spec]
  (-> spec
      json/write-str
      darkstar/vega-lite-spec->svg))

(defn export [spec fmt out-file]
  (log/info (format "Export gantt chart to %s" out-file))
  (condp = fmt
    :svg  (spit out-file (svg spec))
    :json (spit out-file (json spec))
    :html (spit out-file (html spec))
    :png  (-> spec
              svg
              batik/parse-svg-string
              (batik/render-document-to-png out-file))
    (throw (ex-info
             (format "Invalid format %s"
                     (str/upper-case (name fmt)))
             {:fn :deps
              :type type}))))

(defn run [in-file]
  (let [spec (-> (utils/load-edn in-file)
                 vega-lite-spec)]
    (doseq [fmt ["svg" "json" "html"]
            :let [extension (format ".gantt.%s" fmt)]]
      (export spec fmt (str/replace in-file #".edn" extension)))))

(comment
  (oz/start-server!)
  (let [gantt-def (-> (io/resource "data.edn") utils/load-edn)]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
