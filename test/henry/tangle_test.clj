(ns henry.tangle-test
  (:require [clojure.test :refer :all]
            [henry.tangle :as sut]
            [clojure.java.io :as io]))

(deftest test-task->dot-node
  (let [node   {:id       "task_id"
                :label    "task label"
                :duration 3
                :styles   [:tag-c]
                :phase    :phase-name}
        styles {:tag-a {:fillcolor "#FF5555" :style "filled"}
                :tag-b {:fillcolor "#FFAABB" :style "filled"}
                :tag-c {:fillcolor "#00FF00" :style "filled"}}]
    (is (= {:id        "task_id"
            :label     "task label"
            :fillcolor "#00FF00"
            :style     "filled"}
           (sut/task->dot-node node styles)))))

(deftest test-spec->dot
  (let [tasks        [{:id :a :duration 2}
                      {:id :b :duration 1}
                      {:id :c :duration 3}
                      {:id :d :duration 3}
                      {:id :e :duration 1}
                      {:id :f :duration 1}]
        dependencies [[:d :c] [:c :b] [:b :a] [:c :a] [:f :e]]
        spec         {:tasks        tasks
                      :dependencies dependencies}
        expected     (-> "test-spec-to-dot-expected.dot" io/resource slurp)]
    (is (= expected (sut/spec->dot spec)))))
