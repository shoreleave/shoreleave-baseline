(ns shoreleave.pubsubs.protocols)

;; Publish/Subscribe
;; -----------------
;;
;; Shoreleave allows you to compose your programs by
;; declaratively binding functions, atoms, workers, and and localStorage
;; through a publish/subscribe bus.
;;
;; The system is built upon two protocols: IMessageBrokerBus and IPublishable
;;

;; ###IMessageBrokerBus
;; This protocol abstracts away the implementation details of the bus itself,
;; enabling you to program aginst the interface instead of the implementation.
;;
;; You can imagine there different types of buses you might use:
;; local buses, cross-document buses, buses that are a proxy for other servers...
;;
;; The protocol defines the following functions:
;;
;;  * subscribe - for a given bus, bind the handler-fn to the topic.
;;  * subscribe-once - for a given bus, create a one-time binding between the topic and the handler-fn
;;  * subscribe-> - for a given bus, create a chain of subscriptions, similar to how the threading macro works
;;  * unsubscribe - for a given bus, unbind the handler-fn and the topic
;;  * publish - for a given bus, publish data to all the handler-fn's bound to the topic
(defprotocol IMessageBrokerBus
  (subscribe [broker-bus topic handler-fn])
  (subscribe-once [broker-bus topic handler-fn])
  ;(subscribe-> [broker-bus & chain-handler-fns])
  ;; Protocols don't support variable args, back to the old impl
  (subscribe-> [broker-bus handler-fn1 handler-fn2 handler-fn3]
               [broker-bus handler-fn1 handler-fn2 handler-fn3 handler-fn4]
               [broker-bus handler-fn1 handler-fn2 handler-fn3 handler-fn4 handler-fn5])
  (unsubscribe [broker-bus topic handler-fn])
  (publish
    [broker-bus topic data]
    [broker-bus topic data more-data]))

(defn chain-subscriptions [bus & handler-fns]
  (let [subscripts (partition 2 1 handler-fns)]
      (when-not (empty? subscripts)
        (doseq [[t h] subscripts]
          (subscribe bus t h)))))

;; In addition to strings and keywords, functions as topics, allowing for a
;; pipeline-like binding to take place.  For example:
;;
;; `(subscribe my-bus user-search render-search-results)`
;;
;; ... where `user-search` is a function that returns some search results,
;; and `render-search-results` takes search data and renders it to the DOM.
;;
;; When `user-search` is called, the results are automatically rendered to the DOM.
;; This frees the developer from having to liter DOM, view, and state logic
;; throughout the application.  Shoreleave's pubsub allows you to bind together
;; pure functions to build out functionality.
;;
;; Additionally, cross-cutting functionality (like logging) is more easily managed
;; through the pubsub system.

;; ###IPublishable
;; The second protocol is used to define things that can be published,
;; _ie:_ things that be used as a topic.
;;
;; Publishables are usually constructed as a decorator (which result in a
;; Function object containing metadata), or as an adapter to IWatchable interfaces.
;;
;; A Publishable knows how to make a string-based topic of itself (`topicify`),
;; can tell if it has already been decorated (`publishized?`), and can generate
;; the decorator form that is bound to a bus (`publishize`)
(defprotocol IPublishable
  (topicify [t])
  (publishized? [t])
  (publishize [t broker-bus]))

