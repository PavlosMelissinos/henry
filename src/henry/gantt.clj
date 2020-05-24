(ns henry.gantt
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [oz.core :as oz]
            [henry.graph :as graph]
            [henry.tangle :as tangle]))

(defn- duration->end [{:keys [start duration] :as m}]
  (if duration (assoc m :end (+ start duration)) m))

(defn ->gantt [{:keys [tasks dependencies styles] :as cfg}]
  (->> (graph/assign-task-beginnings tasks dependencies)
       (map #(assoc % :label (or (:label %) (:id %))))
       (map #(tangle/style-node % styles))
       (map duration->end)))

(defn run [in-file]
  (let [defaults (-> (io/resource "defaults.edn") slurp edn/read-string)
        cfg      (-> in-file slurp edn/read-string)
        data     (assoc-in cfg [:data :values] (->gantt cfg))
        spec     (merge defaults data)]
    (spit (str/replace in-file #".edn" ".gantt.json")
          (json/write-str spec))
    (oz/export! spec (str/replace in-file #".edn" ".gantt.html"))))

(defn demo []
  (run (io/resource "data.edn")))

(comment
  (oz/start-server!)
  (let [gantt-def (run (io/resource "data.edn"))]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
