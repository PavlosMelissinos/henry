(ns henry.kaocha
  (:require [clojure.spec.test.alpha :as stest]))

(defn post-load-hook [test-plan]
  (println "INSTRUMENTING FUNCTIONS:")
  (prn (stest/instrument))
  test-plan)