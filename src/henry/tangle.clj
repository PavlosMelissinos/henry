(ns henry.tangle
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tangle.core :as tangle]))

(defn style-node [{:keys [styles] :as node} style-def]
  (let [final-style (apply merge (map #(get style-def %) styles))]
    (merge node final-style)))

(defn task->dot-node [task styles]
  (if (keyword? task)
    task
    (-> (style-node task styles)
        (select-keys [:id :label :fillcolor :style]))))     ;TODO: tags missing

(defn task-def->dep-graph [{:keys [tasks styles dependencies] :as tasks-def}]
  (let [node->id         (fn [n] (name (or (:id n) n)))
        node->descriptor (fn [n] (when-not (keyword? n) (update n :id name)))
        options          {:graph            {:rankdir :LR}
                          :directed?        true
                          :node             {:shape :box}
                          :node->id         node->id
                          :node->descriptor node->descriptor
                          }
        stylish-nodes    (map #(task->dot-node % styles) tasks)
        edges            (map (juxt second first) dependencies)]
    (tangle/graph->dot stylish-nodes edges options)))

(defn run [in-file]
  (let [cfg       (-> in-file slurp clojure.edn/read-string)
        dep-graph (task-def->dep-graph cfg)]
    (io/copy (tangle/dot->image dep-graph "png")
             (io/file (str/replace in-file #".edn" ".tasks.png")))))

(defn demo []
  (run (io/resource "data.edn")))
