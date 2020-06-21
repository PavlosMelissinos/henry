(ns henry.core
  (:require [clojure.string :as str]

            [taoensso.timbre :as log]

            [henry.gantt :as gantt]
            [henry.tangle :as deps]))

(defn- deps
  "Given an input spec and a target format, generates a task dependency graph.
   Currently supports the png and dot formats."
  ([spec]
   (deps spec :dot))
  ([spec fmt]
   (log/info "Generating gantt chart...")
   (condp = (keyword fmt)
     :png (deps/png spec)
     :dot (deps/dot spec)
     (throw
       (ex-info
         (format "Invalid format %s" (str/upper-case (name fmt)))
         {:fn   :deps
          :type type})))))

(defn- gantt
  "Given an input spec and a target format, generates a gantt chart.
   Currently supports the svg format."
  ([spec]
   (gantt spec :svg))
  ([spec fmt]
   (log/info (format "Generating gantt chart (%s)..." (name fmt)))
   (condp = (keyword fmt)
     :svg (-> spec gantt/vega-lite-spec gantt/svg)
     (throw (ex-info
              (format "Invalid format %s"
                      (str/upper-case (name fmt)))
              {:fn     :gantt
               :format fmt})))))

(defn convert [spec mode fmt]
  (condp = mode
    :deps (deps spec fmt)
    :gantt (gantt spec fmt)))

(defn export [spec mode fmt out-file]
  (condp = (keyword mode)
    :deps (-> spec deps/dot (deps/export out-file))
    :gantt (-> spec gantt/vega-lite-spec (gantt/export fmt out-file))
    (throw (ex-info
             (format "Invalid mode %s"
                     (str/upper-case (name mode)))
             {:fn   :export
              :mode mode}))))
