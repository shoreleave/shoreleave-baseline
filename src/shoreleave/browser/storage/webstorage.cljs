(ns shoreleave.browser.storage.webstorage
  "An idiomatic interface to the browser's storage mechanisms (local and sessions)"
  (:require [cljs.reader :as reader]
            [goog.storage.mechanism.HTML5WebStorage :as html5webstorage]
            [goog.iter :as g-iter]))

;; Google Closure attaches a common prototype to all browser storage systems called, `WebStorage`.
;; Shoreleave extends this type, to extend ClojureScript functionality/interop to all browsers storages.

;; WebStorage support
;; ----------------------
;;
;; For general information on localStorage, please see the docs in `localstorage.cljs`
;; For general information on sessionStorage, please see the docs in `sessionstorage.cljs`
;;
;; Shoreleave's generic storage support is built against Closure's [interface](http://closure-library.googlecode.com/svn/docs/class_goog_storage_mechanism_HTML5WebStorage.html)
;;
;; The extension supports the following calls:
;;
;;  * map-style lookup - `(:search-results storage "default value")`
;;  * `get` lookups
;;  * `(count storage)` - the number of things/keys stored
;;  * `(assoc! storage :new-key "saved")` - update or add an item
;;  * `(dissoc! storage :saved-results)` - remove an item
;;  * `(empty! storage)` - Clear out the localStorage store
;;
;;
;; Using storage in Pub/Sub
;; -----------------------------
;;
;; There is PubSub support for the specific storage types.
;; Please see the details in those files.
;; You'll need to require them directly to get support.

(defn storage-keys [ls]
  (g-iter/toArray (.__iterator__ ls true)))
(defn storage-values [ls]
  (g-iter/toArray (.__iterator__ ls false)))
(defn as-hash-map
  ([storage]
   (zipmap (storage-keys storage) (storage-values storage))))
 
(extend-type goog.storage.mechanism.HTML5WebStorage
  
  ILookup
  (-lookup
    ([ls k]
      (-lookup ls k nil))
    ([ls k not-found]
      (let [read-value (if-let [v (not-empty (.get ls (name k)))]
                        v
                        (pr-str not-found))]
        (reader/read-string read-value))))

  ISeqable
  (-seq [ls]
    (map vector (storage-keys ls) (storage-values ls)))

  ICounted
  (-count  [ls] (.getCount ls))

  IFn
  (-invoke
    ([ls k]
      (-lookup ls k))
    ([ls k not-found]
      (-lookup ls k not-found))) 

  ITransientCollection
  (-persistent! [ls] (as-hash-map ls))
  ;(-conj! [c v] nil)

  ITransientAssociative
  (-assoc! [ls k v]
    (let [old-val (-lookup ls k)]
      (.set ls (name k) (pr-str v))
      (-notify-watches ls {k old-val} {k v})
      ls))

  ITransientMap
  (-dissoc! [ls k]
    (do
      (.remove ls (name k))
      ls))

  ;IPrintable
  ;(-pr-seq  [ls opts]
   ; #_(let  [pr-pair  (fn  [keyval]  (pr-sequential pr-seq "" " " "" opts keyval))]
   ;   (pr-sequential pr-pair "{" ", " "}" opts ls))
   ; (-pr-seq (-persistent! ls) opts))
  IPrintWithWriter
  (-pr-writer [ls writer opts]
    (let [pers-st (-persistent! ls)]
     (-write writer (-persistent! ls)))))


(defn empty!
  "Clear the storage"
  [ls]
  (.clear ls))

