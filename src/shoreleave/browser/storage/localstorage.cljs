(ns shoreleave.browser.storage.localstorage
  "An idiomatic interface to the browser's local storage"
  (:require [cljs.reader :as reader]
            [goog.storage.mechanism.HTML5LocalStorage :as html5ls]
            [shoreleave.browser.storage.webstorage]))

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
;;
;;
;; Using localStorage in Pub/Sub
;; -----------------------------
;;
;; The apprpriate IWatchable support is attached to Google's HTML5LocalStorage
;; to allow it to participate in Shoreleave's pub/sub system
;;
;; To enable it, you need to `(publishable/include-localstorage!)` in the file
;; where you setup and wire together your bus and publishables.

(extend-type goog.storage.mechanism.HTML5LocalStorage

  IWatchable
  (-notify-watches [ls oldval newval]
    (doseq  [[key f] @ls-watchers]
      (f key ls oldval newval)))
  (-add-watch [ls key f]
    (swap! ls-watchers assoc key f))
  (-remove-watch [ls key]
    (swap! ls-watchers dissoc key)))


;; ###Usage
;; You'll typically do something like: `(def local-storage (localstorage/storage)`
(defn storage
  "Get the browser's localStorage"
  []
  (goog.storage.mechanism.HTML5LocalStorage.))

;;Much like how you can easily get "cookies/cookies" you can get "localstorage/localstorage"
(def localstorage (storage))

