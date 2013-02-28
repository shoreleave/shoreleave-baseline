(ns shoreleave.pubsubs.publishable
  (:require [shoreleave.pubsubs.protocols :as ps-protocols]))

;; Publishables
;; ------------
;; Shoreleave comes with out-of-the-box support for the most common
;; publishables
;;
;; ###Functions and Function types
;; Functions need to be decorated (much like how `memoize` works).
;; This is now supported by CLJS core - ported from Shoreleave.
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
;; ###Browser storages (localStorage and sessionStorage)
;; All storage systems behave exactly like an atom, as described above
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

  ;; this could be a Fn that we attached metadata to - for some reason it gets hit like an obj, instead of a fn 
  object
  (topicify [t]
    (or (ps-protocols/publishized? t)
        (-> t hash str)))
  (publishized? [t]
    (:sl-published (meta t)))
  
  string
  (topicify [t] t)

  default
  (topicify [t]
    (name t)))

;; Local Storage
;; -------------
;; It is expected that before calling this, you've handled your depenencies, ala
;;  `(:require [goog.storage.mechanism.HTML5LocalStorage :as gls])`
(defn include-localstorage! []
  (extend-type goog.storage.mechanism.HTML5LocalStorage

    ps-protocols/IPublishable
    (topicify [t]
      (or (ps-protocols/publishized? t)
          (-> t hash str)))
    (publishized? [t]
      (-> t hash str))
    (publishize [ls-as-topic bus]
      (let [published-topic (ps-protocols/topicify ls-as-topic)
            bus-key (-> bus hash keyword)]
        (do
          (add-watch ls-as-topic bus-key #(ps-protocols/publish bus published-topic {:old %3 :new %4}))
          ls-as-topic)))))

;; Session Storage
;; -------------
;; It is expected that before calling this, you've handled your depenencies, ala
;;  `(:require [goog.storage.mechanism.HTML5SessionStorage :as glss])`
(defn include-sessionstorage! []
  (extend-type goog.storage.mechanism.HTML5SessionStorage

    ps-protocols/IPublishable
    (topicify [t]
      (or (ps-protocols/publishized? t)
          (-> t hash str)))
    (publishized? [t]
      (-> t hash str))
    (publishize [ss-as-topic bus]
      (let [published-topic (ps-protocols/topicify ss-as-topic)
            bus-key (-> bus hash keyword)]
        (do
          (add-watch ss-as-topic bus-key #(ps-protocols/publish bus published-topic {:old %3 :new %4}))
          ss-as-topic)))))


;; This is left in the code for historical reasons,
;; You can use it as an example on how to build custom
;; function decorators in ClojureScript, correctly use the Blob system,
;; generate new Object URLs, and how WebWorkers execute.
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
 
