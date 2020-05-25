(ns henry.core
  (:require [henry.tangle :as tasks]
            [henry.gantt :as gantt]
            [henry.utils :as utils]))

(def load-edn utils/load-edn)

(def load-gantt-config gantt/load-config)

(defn build [mode cfg]
  (condp = mode
    :tasks (tasks/task-def->dep-graph cfg)
    :gantt (gantt/convert cfg)))

(def tasks->png tasks/export)

(def gantt->html gantt/->html)

(def gantt->json gantt/->json)
