(ns henry.repl
  (:require [henry.core :as henry]
            [clojure.java.io :as io]))

(comment
  (def spec (-> "ml-data.edn"
                io/resource
                henry/load-edn))

  (henry/convert spec :deps :png)
  (henry/convert spec :gantt :svg)

  (henry/export spec :deps :png "ml-data.deps.png")
  (henry/export spec :gantt :svg "ml-data.gantt.svg"))