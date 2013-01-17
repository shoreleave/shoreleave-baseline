(ns shoreleave.pubsubs.event
  "An extended pub/sub implementation built on Google's EventTarget and Event system"
  (:require [goog.events :as gevents]
            [goog.events.EventTarget :as gevent-target]
            [clojure.browser.event :as event]
            [shoreleave.pubsubs.protocols :as ps-protocols]))

;; Below is an implementation of IMessageBrokerBus built upon
;; Google Closure's [EventTarget object](http://closure-library.googlecode.com/svn/docs/class_goog_events_EventTarget.html)

;; This effectively makes *ALL* EventTargets capable buses

;; This is the internal Event used for publishing.  It ships data along with it
(defn publish-event [topic data]
  (let [e (goog.events.Event. topic)]
    (set! (.-data e) data)
    e))

;; Because we need to unpack events for the handler functions,
;; We need a mapping of handler-fn -> #(handler-fn (.-data %))
(def handlers (atom {}))

(extend-type goog.events.EventTarget
  ps-protocols/IMessageBrokerBus
  (subscribe [bus topic handler-fn]
    (let [wrapped-handler-fn #(handler-fn (.-data %))]
      (swap! handlers assoc handler-fn wrapped-handler-fn)
      (event/listen bus (ps-protocols/topicify topic) wrapped-handler-fn)))

  (subscribe-once [bus topic handler-fn]
    (let [wrapped-handler-fn #(handler-fn (.-data %))]
      (swap! handlers assoc handler-fn wrapped-handler-fn)
      (event/listen-once bus (ps-protocols/topicify topic) wrapped-handler-fn)))

  (subscribe->
    ([bus handler-fn1 handler-fn2 handler-fn3]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3))
    ([bus handler-fn1 handler-fn2 handler-fn3 handler-fn4]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3 handler-fn4))
    ([bus handler-fn1 handler-fn2 handler-fn3 handler-fn4 handler-fn5]
     (ps-protocols/chain-subscriptions bus handler-fn1 handler-fn2 handler-fn3 handler-fn4 handler-fn5)))

  (unsubscribe [bus topic handler-fn]
    (event/unlisten bus (ps-protocols/topicify topic) (@handlers handler-fn)))

  (publish
    ([bus topic data]
     (event/dispatch-event bus (publish-event
                                 (ps-protocols/topicify topic)
                                 data)))
    ([bus topic data & more-data]
     (event/dispatch-event bus (publish-event
                                 (ps-protocols/topicify topic)
                                 (into [data] more-data)))))

  IHash
  (-hash [bus] (goog.getUid bus)))

(defn bus
  "Get an event bus"
  []
  (goog.events.EventTarget.))

