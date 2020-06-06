(ns henry.tangle
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [henry.utils :as utils]
            [tangle.core :as tangle]))

(defn- task->dot-node [task styles]
  (if (keyword? task)
    task
    (utils/style-node task styles)))

(defn dot [{:keys [tasks styles dependencies] :as spec}]
  (let [node->id         (fn [n] (-> (:id n) (or n) name))
        node->descriptor (fn [n] (when-not (keyword? n) (update n :id name)))
        options          {:graph            {:rankdir :LR}
                          :directed?        true
                          :node             {:shape :box}
                          :node->id         node->id
                          :node->descriptor node->descriptor}
        stylish-nodes    (map #(task->dot-node % styles) tasks)
        edges            (map (juxt second first) dependencies)]
    (tangle/graph->dot stylish-nodes edges options)))

(defn png [spec]
  (-> spec dot (tangle/dot->image "png")))

(defn svg [spec]
  (-> spec dot tangle/dot->svg))

(defn export [dot out-file]
  (io/copy (tangle/dot->image dot "png")
           (io/file out-file)))

(defn run [in-file]
  (let [dep-graph (-> in-file utils/load-edn dot)]
    (export dep-graph (-> in-file
                          io/file
                          (str/replace #".edn" ".tasks.png")))))
