(ns henry.core
  (:require [clojure.string :as str]
            [henry.gantt :as gantt]
            [henry.tangle :as deps]
            [henry.utils :as utils]))

(def load-edn utils/load-edn)

(defn- deps
  "Generates a task dependency graph from the input spec into the specified format.
   Currently supports the png and dot formats."
  ([spec]
   (deps spec :dot))
  ([spec format]
  (condp = (keyword format)
    :png (deps/png spec)
    :dot (deps/dot spec)
    (throw (ex-info
             (format "Invalid format %s"
                     (str/upper-case (name format)))
             {:fn :deps
              :type type})))))

(defn- gantt
  "Generates a gantt chart from the input spec into the specified format.
   Currently supports the svg format."
  [spec format]
  (condp = (keyword format)
    :svg  (-> spec gantt/vega-lite-spec gantt/svg)
    (throw (ex-info
             (format "Invalid format %s"
                     (str/upper-case (name format)))
             {:fn :gantt
              :format format}))))

(defn convert [spec mode format]
  (condp = mode
    :deps  (deps spec format)
    :gantt (gantt spec format)))

(defn export [spec mode format out-file]
  (condp = (keyword mode)
    :deps  (-> spec deps/dot (deps/export out-file))
    :gantt (-> spec gantt/vega-lite-spec (gantt/export format out-file))
    (throw (ex-info
             (format "Invalid mode %s"
                     (str/upper-case (name mode)))
             {:fn :export
              :mode mode}))))
