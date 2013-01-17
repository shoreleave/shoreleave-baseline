(ns shoreleave.pubsubs.simple
  "An extended pub/sub implementation built on Google's PubSub Object"
  (:require [goog.pubsub.PubSub :as pubsub]
            [shoreleave.pubsubs.protocols :as ps-protocols]))

;; Below is an implementation of IMessageBrokerBus built upon
;; Google Closure's [PubSub object](http://closure-library.googlecode.com/svn/docs/class_goog_pubsub_PubSub.html)
;; This will have all the properties of PubSub (ie: synchronous)

(extend-type goog.pubsub.PubSub
  ps-protocols/IMessageBrokerBus
  (subscribe [bus topic handler-fn]
    (.subscribe bus (ps-protocols/topicify topic) handler-fn))

  (subscribe-once [bus topic handler-fn]
    (.subscribeOnce bus (ps-protocols/topicify topic) handler-fn))

  (subscribe->
    ([bus handler-fn1 handler-fn2 handler-fn3]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3))
    ([bus handler-fn1 handler-fn2 handler-fn3 handler-fn4]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3 handler-fn4))
    ([bus handler-fn1 handler-fn2 handler-fn3 handler-fn4 handler-fn5]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3 handler-fn4 handler-fn5)))

  (unsubscribe [bus topic handler-fn]
    (.unsubscribe bus (ps-protocols/topicify topic) handler-fn))

  (publish
    ([bus topic data]
     (.publish bus (ps-protocols/topicify topic) data))
    ([bus topic data & more-data]
     (.publish bus (ps-protocols/topicify topic) (into [data] more-data))))

  IHash
  (-hash [bus] (goog.getUid bus)))

(defn subscribers-count
  "Given a bus and a topic, return the number of subscribers
  (registered handler functions)"
  [bus topic]
  (.getCount bus (ps-protocols/topicify topic)))

(defn bus
  "Get a simple bus"
  []
  (goog.pubsub.PubSub.))

