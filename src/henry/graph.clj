(ns henry.graph
  (:require [ubergraph.core :as uber]
            [weavejester.dependency :as dep]))

(defn node-attrs
  ([g] (node-attrs g (dep/nodes g)))
  ([g & nodes] (set (map #(get-in g [:node-attrs %]) nodes))))

(defn predecessors [g node]
  (-> g :dependencies (get node)))

(defn attrs [g node]
  (-> g :node-attrs (get node)))

(defn validate-tasks [tasks]
  (let [task-ids (map :id tasks)
        freqs    (frequencies task-ids)]
    (when (not (apply distinct? task-ids))
      (throw
        (ex-data {:ex "Duplicate task ids"
                  :duplicates (into {} (filter #(> (second %) 1) freqs))})))))


(defn build [tasks dependencies]
  "Makes a dependency graph from the given collection of tasks"
  (validate-tasks tasks)
  (let [node-attrs (reduce-kv (fn [m k v]
                                (assoc m k (first v)))
                              {}
                              (group-by :id tasks))
        edges      (map (juxt second first) dependencies)
        depgraph   (reduce (fn [g [dependant dependency]]
                             (dep/depend g dependant dependency))
                           (dep/graph)
                           dependencies)]
    (assoc depgraph :node-attrs node-attrs
                    :nodes (map :id tasks)
                    :edges edges)))

(defn node-end [{:keys [start duration] :as n}]
  (if n
    (+ (or start 0) (or duration 1))
    0))

(defn assign-task-beginnings [graph]
  (loop [[n & n-tail] (dep/topo-sort graph)
         g            graph]
    (if-not n
      (set (map (partial attrs g) (dep/nodes g)))
      (let [n-predecessors   (predecessors g n)
            predecessor-ends (->> (apply node-attrs g n-predecessors)
                                  (map node-end))
            max-predecessor-end (apply max (conj predecessor-ends 0))]
        (recur n-tail
               (assoc-in g [:node-attrs n :start] max-predecessor-end))))))
