(ns henry.main
  (:require [oz.core :as oz]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn duration->finish [{:keys [start duration] :as m}]
  (if duration (assoc m :finish (+ start duration)) m))

(defn load-edn [filename]
  (-> filename io/resource slurp edn/read-string))

(defn load-config []
  (let [cfg  (load-edn "config.edn")
        data (->> (load-edn "data.edn")
                  (map duration->finish))]
    (assoc-in cfg [:data :values] data)))

(defn main- []
  (oz/start-server!))

(comment
  (let [gantt-def (load-config)]
    (oz/view! gantt-def))
  (oz/view! (load-config)))
