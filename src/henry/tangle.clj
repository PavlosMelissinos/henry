(ns henry.tangle
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tangle.core :as tangle]
            [henry.utils :as utils]))

(defn task->dot-node [task styles]
  (if (keyword? task)
    task
    (utils/style-node task styles)))

(defn spec->dot [{:keys [tasks styles dependencies] :as spec}]
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

(defn spec->png [spec]
  (-> spec spec->dot (tangle/dot->image "png")))

#_(defn spec->svg [spec]
  (-> spec spec->dot tangle/dot->svg))

(defn export [dot out-file]
  (io/copy (tangle/dot->image dot "png")
           (io/file out-file)))

(defn run [in-file]
  (let [dep-graph (-> in-file utils/load-edn spec->dot)]
    (export dep-graph (-> in-file
                          io/file
                          (str/replace #".edn" ".tasks.png")))))

(defn demo []
  (run (io/resource "data.edn")))
