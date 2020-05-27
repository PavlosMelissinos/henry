(ns henry.utils-test
  (:require [clojure.test :refer :all]
            [henry.utils :as sut]))

(deftest test-style-node
  (let [node   {:id        "task_id"
                :label     "task label"
                :duration  3
                :styles    [:tag-c]
                :phase     :phase-name}
        styles {:tag-a {:fillcolor "#FF5555" :style "filled"}
                :tag-b {:fillcolor "#FFAABB" :style "filled"}
                :tag-c {:fillcolor "#00FF00" :style "filled"}}]
    (is (= {:id        "task_id"
            :label     "task label"
            :duration  3
            :fillcolor "#00FF00"
            :style     "filled"
            :styles    [:tag-c]
            :phase     :phase-name}
           (sut/style-node node styles)))))