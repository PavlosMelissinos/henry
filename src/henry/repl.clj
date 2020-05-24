(ns henry.repl
  (:require [henry.tangle :as tasks]
            [henry.gantt :as gantt]))

(def modes [:tasks :gantt])

(defn run [mode in-file]
  (condp = mode
    :tasks (tasks/run in-file)
    :gantt (gantt/run in-file)))


(comment
  (run :tasks "test_resources/ml-data.edn")
  (run :gantt "test_resources/ml-data.edn")

  (map #(run % "/home/pavlos/Data/henry/ml-data.edn") modes))