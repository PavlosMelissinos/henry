(ns henry.graph
  (:require [ubergraph.core :as uber]
            [ubergraph.alg :as alg]
            [cljol.ubergraph-extras :as uber-extras]))

(defn node-attrs [g & nodes]
  (map (partial uber/attrs g) nodes))

(defn- prepare [tasks dependencies]
  (let [nodes (map (juxt :id identity) tasks)
        edges (map (juxt second first) dependencies)]
    (-> (uber/digraph)
        (uber/add-nodes-with-attrs* nodes)
        (uber/add-edges* edges))))

(defn node-end [{:keys [id start duration] :as n}]
  (+ (or start 0) (or duration 1)))

(defn assign-task-beginnings [tasks dependencies]
  (let [graph (prepare tasks dependencies)
        {:keys [topological-order]} (uber-extras/topsort2 graph)]
    (loop [[n & n-tail] topological-order
           g        graph]
      (if-not n
        (map (partial uber/attrs g) (uber/nodes g))
        (let [n-predecessors      (uber/predecessors g n)
              predecessor-ends (->> (apply node-attrs g n-predecessors)
                                    (map node-end))
              max-predecessor-end (apply max (conj predecessor-ends 0))
              n-attrs             (assoc (uber/attrs g n) :start max-predecessor-end)]
          (recur n-tail
                 (uber/add-nodes-with-attrs g [n n-attrs])))))))

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
    (-> (apply uber/digraph edges)
        (uber-extras/topsort2 {:sorted-in-edges true}))))
