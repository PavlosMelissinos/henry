(ns henry.core
  (:require [henry.tangle :as deps]
            [henry.gantt :as gantt]
            [henry.utils :as utils]))

(def load-edn utils/load-edn)

(defn build [mode cfg]
  (condp = mode
    :tasks (deps/spec->dot cfg)
    :gantt (gantt/->vega-lite-spec cfg)))

(def tasks->png deps/export)

(def gantt->html gantt/->html)

(def gantt->json gantt/->json)

(def gantt->svg gantt/->svg)

(defn ->dot
  "Generates a dot formatted string from the input spec.
   Only supports dependency graphs, use `->png` or `->svg` to generate
   a gantt chart"
  [spec type]
  (condp = type
    :deps  (deps/spec->dot spec)
    (throw (ex-info
             (format "Dot file generation not supported for %s" (name type))
             {:fn :->dot
              :type type}))))

(defn ->png
  "Generates a dot formatted string from the input spec.
   Type can either be :deps or :gantt"
  [spec type]
  (condp = type
    :deps  (deps/spec->png spec)
    :gantt
    (throw (ex-info
             (format "Dot file generation not supported for %s" (name type))
             {:fn :->png
              :type type}))))

(defn ->svg
  "Generates a dot formatted string from the input spec.
   Only supports the creation of gantt charts, use `->dot` or `->png` to generate
   a task dependency graph"
  [spec type]
  (condp = type
    :gantt  (gantt/)
    (throw (ex-info
             (format "Dot file generation not supported for %s" (name type))
             {:fn :->svg
              :type type}))))

(def ->svg2 ->svg)