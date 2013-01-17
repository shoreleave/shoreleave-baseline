(ns shoreleave.pubsub.publishable
  (:require [shoreleave.pubsubs.protocols :as ps-protocols]))

;; Publishables
;; ------------
;; Shoreleave comes with out-of-the-box support for the most common
;; publishables
;;
;; ###Functions and Function types
;; Functions need to be decorated (much like how `memoize` works).
;;
;; For example:
;;
;;      (def a-bus (simple-bus/bus))
;;      (defn some-fn [] 5)
;;      (def some-fn-p (publishize some-fn))
;;
;;      (defn another-fn [x] (inc x))
;;      (def another-fn-p (publishize another-fn))
;;
;;      (subscribe a-bus another-fn-p some-fn-p) ;; some-fn-p will automatically publish its result to another-fn-p
;;      ;; Note that the subscribing function (or entity) doesn't need to be `publishize`
;;
;;      (some-fn) ;; This DOES NOT get sent to the bus
;;      (some-fn-p) ;; The results of this call are published on the bus
;;
;; Anything that is subscribed to `some-fn-p` will get the value `5` when the
;; function is called, as shown above.
;;
;; ###Atoms
;; Atoms can also be topics.  This is no different than _watching_ the atom.
;; 
;; All subscribed functions will be passed a map: `{:old some-val :new another-val}`
;;
;; ###LocalStorage
;; localStorage behaves exactly like an atom, as described above
;;
;; ###WorkerFn
;; Embedded workers behave exactly like atoms, as described above
;;
;; ###The default case
;; You can also use strings and keywords as topics (the most useful case for
;; cross-cutting functionality).
(extend-protocol ps-protocols/IPublishable

  function
  (topicify [t]
    (or (ps-protocols/publishized? t)
        (-> t hash str)))
  (publishized? [t]
    (:sl-published (meta t)))
  (publishize [fn-as-topic bus]
    (if (-> (meta fn-as-topic) :sl-buses (get (-> bus hash keyword)))
      fn-as-topic
      (let [published-topic (ps-protocols/topicify fn-as-topic)
            new-meta (assoc (meta fn-as-topic) :sl-published published-topic
                                               :sl-buses (-> (get (meta fn-as-topic) :sl-buses #{}) (conj (-> bus hash keyword))))]
        (with-meta (fn [& args] (let [ret (apply fn-as-topic args)]
                       (ps-protocols/publish bus published-topic ret)
                       ret))
                   new-meta))))

  Atom
  (topicify [t]
    (or (ps-protocols/publishized? t)
        (-> t hash str)))
  (publishized? [t]
    (-> t hash str))
  (publishize [atom-as-topic bus]
    (let [published-topic (ps-protocols/topicify atom-as-topic)
          bus-key (-> bus hash keyword)]
      (do
        (add-watch atom-as-topic bus-key #(ps-protocols/publish bus published-topic {:old %3 :new %4}))
        atom-as-topic)))

 
  default
  (topicify [t]
    (str t)))

(defn include-localstorage []
 ;; This is currently removed until a cross-browser (or goog-jar) approach is stablized
  ;(extend-protocol ps-protocols/IPublishable

  ;  js/localStorage
  ;  (topicify [t]
  ;    (or (ps-protocols/publishized? t)
  ;        (-> t hash str)))
  ;  (ps-protocols/publishized? [t]
  ;    (-> t hash str))
  ;  (publishize [ls-as-topic bus]
  ;    (let [published-topic (topicify ls-as-topic)
  ;          bus-key (-> bus hash keyword)]
  ;      (do
  ;        (add-watch ls-as-topic bus-key #(publish bus published-topic {:old %3 :new %4}))
  ;        ls-as-topic))))
  )

;; TODO remove this - it was a bad idea
#_(defn include-workers
  "Allow WebWorkers to participate in the PubSub system
  NOTE: This means your browser supports BlobBuilder or Blob"
  []
  (do
    (require '[shoreleave.worker :as swk])
    (extend-protocol IPublishable
      swk/WorkerFn
      (topicify [t]
        (or (ps-protocols/publishized? t)
            (-> t hash str)))
      (publishized? [t]
        (-> t hash str))
      (publishize [worker-as-topic bus]
        (let [published-topic (topicify worker-as-topic)
              bus-key (-> bus hash keyword)]
          (do
            (add-watch worker-as-topic bus-key #(publish bus published-topic {:old %3 :new %4}))
            worker-as-topic))))
    true))
 
