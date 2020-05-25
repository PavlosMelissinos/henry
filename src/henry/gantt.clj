(ns henry.gantt
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [oz.core :as oz]
            [henry.graph :as graph]
            [henry.utils :as utils]))

(defn load-config [filename]
  (let [defaults (utils/load-edn (io/resource "defaults.edn"))
        cfg      (utils/load-edn filename)]
    (merge defaults cfg)))

(defn- duration->end [{:keys [start duration] :as m}]
  (if duration (assoc m :end (+ start duration)) m))

(defn convert [{:keys [tasks dependencies styles] :as cfg}]
  (let [tasks (->> (graph/assign-task-beginnings tasks dependencies)
                   (map #(assoc % :label (or (:label %) (:id %))))
                   (map #(utils/style-node % styles))
                   (map duration->end))]
    (-> (assoc-in cfg [:data :values] tasks)
        (dissoc :tasks :dependencies))))

(defn ->html [spec out-file]
  (oz/export! spec out-file))

(defn ->json [spec out-file]
  (spit out-file (json/write-str spec)))

(defn run [in-file]
  (let [cfg  (load-config in-file)
        spec (convert cfg)]
    (->json spec (str/replace in-file #".edn" ".gantt.json"))
    (->html spec (str/replace in-file #".edn" ".gantt.html"))))

(defn demo []
  (run (io/resource "data.edn")))

(comment
  (oz/start-server!)
  (let [gantt-def (run (io/resource "data.edn"))]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
