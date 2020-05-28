(ns henry.repl
  (:require [henry.core :as henry]))

(def modes [:tasks :gantt])

(defn run [mode in-file]
  (condp = mode
    :tasks (henry/build :tasks (henry/load-edn in-file))
    :gantt (henry/build :gantt (henry/load-edn in-file))))


(comment
  (run :tasks "test_resources/ml_data.edn")
  (run :gantt "test_resources/ml_data.edn")

  (doseq [mode modes]
    (run mode "/home/pavlos/Data/henry/ml_data.edn")))