(ns henry.graph
  (:require [ubergraph.core :as uber]
            [weavejester.dependency :as dep]))

(defn node-attrs [g & nodes]
  (map (partial get g) nodes))

(defn nodes [g]
  (->> g :nodes (map first)))

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
        depgraph   (reduce (fn [g [dependant dependency]] (dep/depend g dependant dependency))
                           (dep/graph)
                           edges)]
    (assoc depgraph :node-attrs node-attrs
                    :nodes (map :id tasks)
                    :edges edges)))

(defn node-end [{:keys [start duration] :as n}]
  (+ (or start 0) (or duration 1)))

(defn assign-task-beginnings [graph]
  (loop [[n & n-tail] (dep/topo-sort graph)
         g            graph]
    (if-not n
      (map (partial attrs g) (nodes g))
      (let [n-predecessors   (predecessors g n)
            predecessor-ends (->> (apply node-attrs g n-predecessors)
                                  (map node-end))
            max-predecessor-end (apply max (conj predecessor-ends 0))
            n-attrs             (assoc (attrs g n) :start max-predecessor-end)]
        (recur n-tail
               (uber/add-nodes-with-attrs g [n n-attrs]))))))

(defn run [in-file out-file])

(defn main []
  (let [edges [[:dash_model_list :ml_more_info_endpoints]

               [:vc_sqs_utils :queues_local_dev]
               [:ml_sqs_utils :queues_local_dev]

               [:aws_scheduled_triggers_setup :aws_sqs_setup]
               [:aws_scheduled_triggers_setup :align_envs]
               [:basic_automation :vc_sqs_utils]
               [:basic_automation :ml_sqs_utils]
               [:basic_automation :aws_scheduled_triggers_setup]

               [:dash_manual_triggering :basic_automation]
               [:dash_manual_triggering :dash_sqs_utils]
               [:dash_manual_triggering :dash_users]

               [:dash_ml_schedule_admin :basic_automation]
               [:dash_ml_schedule_admin :unknown]

               [:dash_slack_notification :dash_sqs_utils]
               [:dash_slack_notification :basic_automation]]]
    (dep/topo-sort (build nil edges))))
