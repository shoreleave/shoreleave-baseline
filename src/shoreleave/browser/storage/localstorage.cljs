(ns shoreleave.browser.storage.localstorage
  "An idiomatic interface to the browser's local storage"
  (:require [cljs.reader :as reader]
            [goog.storage.mechanism.HTML5LocalStorage :as html5ls]))

;; Watchers
;; --------
;;
;; In most applications, you want to trigger actions when data is changed.
;; To support this, Shoreleave's local storage use IWatchable and maintains
;; the watchers in an atom.

(def ls-watchers (atom {}))

;; `localStorage` support
;; ----------------------
;;
;; For general information on localStorage, please see [Mozilla's docs](https://developer.mozilla.org/en/DOM/Storage#localStorage)
;;
;; Shoreleave's localStorage support is built against Closure's [interface](http://closure-library.googlecode.com/svn/docs/class_goog_storage_mechanism_HTML5LocalStorage.html)
;;
;; The extension supports the following calls:
;;
;;  * map-style lookup - `(:search-results local-storage "default value")`
;;  * `get` lookups
;;  * `(count local-storage)` - the number of things/keys stored
;;  * `(assoc! local-storage :new-key "saved")` - update or add an item
;;  * `(dissoc! local-storage :saved-results)` - remove an item
;;  * `(empty! local-storage)` - Clear out the localStorage store

(extend-type goog.storage.mechanism.HTML5LocalStorage
  
  ILookup
  (-lookup
    ([ls k]
      (-lookup ls k nil))
    ([ls k not-found]
      (reader/read-string (.get ls (name k) not-found))))

  ICounted
  (-count  [ls] (.getCount ls))

  IFn
  (-invoke
    ([ls k]
      (-lookup ls k))
    ([ls k not-found]
      (-lookup ls k not-found))) 

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

  IWatchable
  (-notify-watches [ls oldval newval]
    (doseq  [[key f] @ls-watchers]
      (f key ls oldval newval)))
  (-add-watch [ls key f]
    (swap! ls-watchers assoc key f))
  (-remove-watch [ls key]
    (swap! ls-watchers dissoc key))

  ;IPrintable
  ;(-pr-seq  [c opts]
   ; #_(let  [pr-pair  (fn  [keyval]  (pr-sequential pr-seq "" " " "" opts keyval))]
   ;   (pr-sequential pr-pair "{" ", " "}" opts c))
   ; (-pr-seq (-persistent! c) opts))
)

(defn empty!
  "Clear the localStorage"
  [ls]
  (.clear ls))

;; ###Usage
;; You'll typically do something like: `(def local-storage (localstorage/storage)`
(defn storage
  "Get the browser's localStorage"
  []
  (goog.storage.mechanism.HTML5LocalStorage.))

