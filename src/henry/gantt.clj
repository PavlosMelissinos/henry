(ns henry.gantt
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]

            [applied-science.darkstar :as darkstar]
            [oz.core :as oz]
            [henry.graph :as graph]
            [henry.utils :as utils]))

(def default-cfg (utils/load-edn (io/resource "defaults.edn")))

(defn- duration->end [{:keys [start duration] :as m}]
  (if duration (assoc m :end (+ start duration)) m))

(defn vega-lite-spec [{:keys [tasks dependencies styles] :as cfg}]
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

(defn export [spec format out-file]
  (condp = format
    "svg"  (spit out-file (svg spec))
    "json" (spit out-file (json spec))
    "html" (spit out-file (html spec))))

(defn run [in-file]
  (let [spec (-> (utils/load-edn in-file)
                 vega-lite-spec)]
    (doseq [format ["svg" "json" "html"]
            :let [extension (format ".gantt.%s" format)]]
      (export spec format (str/replace in-file #".edn" extension)))))

(defn demo []
  (run (io/resource "data.edn")))

(comment
  (oz/start-server!)
  (let [gantt-def (-> (io/resource "data.edn") utils/load-edn)]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
