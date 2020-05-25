(ns henry.utils
  (:require [clojure.edn :as edn]))

(defn load-edn [filename]
  (-> filename slurp edn/read-string))

(defn style-node [{:keys [styles] :as node} style-def]
  (let [final-style (apply merge (map #(get style-def %) styles))]
    (merge node final-style)))
