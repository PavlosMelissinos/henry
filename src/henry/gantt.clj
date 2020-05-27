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

(defn convert [{:keys [tasks dependencies styles] :as cfg}]
  (let [tasks (->> (graph/assign-task-beginnings tasks dependencies)
                   (map #(assoc % :label (or (:label %) (:id %))))
                   (map #(utils/style-node % styles))
                   (map duration->end))]
    (-> (merge default-cfg cfg)
        (assoc-in [:data :values] tasks)
        (dissoc :tasks :dependencies))))

(defn ->html [spec out-file]
  (oz/export! spec out-file))

(defn ->json [spec out-file]
  (spit out-file (json/write-str spec)))

(defn ->svg [spec out-file]
  (-> spec
      json/write-str
      darkstar/vega-lite-spec->svg
      (partial spit out-file)))

(defn run [in-file]
  (let [cfg  (utils/load-edn in-file)
        spec (convert cfg)]
    (->json spec (str/replace in-file #".edn" ".gantt.json"))
    (->html spec (str/replace in-file #".edn" ".gantt.html"))
    (->svg spec (str/replace in-file #".edn" ".gantt.svg"))))

(defn demo []
  (run (io/resource "data.edn")))

(comment
  (oz/start-server!)
  (let [gantt-def (-> (io/resource "data.edn") utils/load-edn)]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
