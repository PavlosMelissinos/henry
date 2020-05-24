(ns henry.graph-test
  (:require [clojure.test :refer :all]
            [henry.graph :as sut]
            [ubergraph.core :as uber]
            [ubergraph.alg :as uberalg]
            [cljol.ubergraph-extras :as uber-extras]))


(deftest test-node-end
  (let [nodes [{:id :e, :duration 1, :start 0}
               {:id :f, :duration 1, :start 1}
               {:id :a, :duration 2, :start 0}
               {:id :b, :duration 1, :start 2}
               {:id :c, :duration 3, :start 2}
               {:id :d, :duration 3, :start 3}]]
    (is (= [1 2 2 3 5 6]
           (map sut/node-end nodes)))))

(deftest test-assign-task-beginnings
  (let [tasks        [{:id :a :duration 2}
                      {:id :b :duration 1}
                      {:id :c :duration 3}
                      {:id :d :duration 3}
                      {:id :e :duration 1}
                      {:id :f :duration 1}]
        dependencies [[:d :c] [:c :b] [:b :a] [:c :a] [:f :e]]]
    (is (= [{:id :a :duration 2 :start 0}
            {:id :b :duration 1 :start 2}
            {:id :c :duration 3 :start 3}
            {:id :d :duration 3 :start 6}
            {:id :e :duration 1 :start 0}
            {:id :f :duration 1 :start 1}]
           (sut/assign-task-beginnings tasks dependencies)))))
