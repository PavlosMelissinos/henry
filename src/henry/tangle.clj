(ns henry.tangle
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tangle.core :as tangle]
            [henry.utils :as utils]))

(defn task->dot-node [task styles]
  (if (keyword? task)
    task
    (-> (utils/style-node task styles)
        (select-keys [:id :label :fillcolor :style]))))     ;TODO: tags missing

(defn task-def->dep-graph [{:keys [tasks styles dependencies] :as tasks-def}]
  (let [node->id         (fn [n] (name (or (:id n) n)))
        node->descriptor (fn [n] (when-not (keyword? n) (update n :id name)))
        options          {:graph            {:rankdir :LR}
                          :directed?        true
                          :node             {:shape :box}
                          :node->id         node->id
                          :node->descriptor node->descriptor}
        stylish-nodes    (map #(task->dot-node % styles) tasks)
        edges            (map (juxt second first) dependencies)]
    (tangle/graph->dot stylish-nodes edges options)))

(defn export [dep-graph out-file]
  (io/copy (tangle/dot->image dep-graph "png")
           (io/file out-file)))

(defn run [in-file]
  (let [cfg       (-> in-file utils/load-edn)
        dep-graph (task-def->dep-graph cfg)]
    (export dep-graph (str/replace in-file #".edn" ".tasks.png"))))

(defn demo []
  (run (io/resource "data.edn")))
