(ns henry.config
  (:require [clojure.spec.alpha :as s]))

(s/def ::description string?)


(s/def ::task-id keyword?)
(s/def ::task-label string?)
(s/def ::task-duration number?)

(s/def ::task-style keyword?)
(s/def ::task-styles (s/coll-of ::task-style :distinct true :into []))

(s/def ::task (s/keys :req-un [::task-id]
                      :opt-un [::task-label ::task-duration ::task-styles]))

(s/def ::dependency (s/coll-of keyword? :distinct true :into []))
(s/def ::dependencies (s/coll-of ::dependency :distinct true :into []))
(s/def ::fillcolor string?)
(s/def ::style string?)
(s/def ::style-def (s/keys :opt-un [::fillcolor ::style]))
(s/def ::styles (s/map-of keyword? ::style-def))

(s/def ::config (s/keys :req-un [::description ::tasks ::dependencies]
                        :opt-un [::styles]))
