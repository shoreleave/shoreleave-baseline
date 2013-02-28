(ns shoreleave.browser.storage.sessionstorage
  "An idiomatic interface to the browser's session storage"
  (:require [cljs.reader :as reader]
            [goog.storage.mechanism.HTML5SessionStorage :as html5ss]
            [shoreleave.browser.storage.webstorage]))

;; Watchers
;; --------
;;
;; In most applications, you want to trigger actions when data is changed.
;; To support this, Shoreleave's session storage use IWatchable and maintains
;; the watchers in an atom.  This is identical to techniques used in local storage.

(def ss-watchers (atom {}))

;; `sessionStorage` support
;; ----------------------
;;
;; For general information on sessionStorage, please see [Mozilla's docs](https://developer.mozilla.org/en/DOM/Storage#sessionStorage)
;;
;; Shoreleave's sessionStorage support is built against Closure's [interface](http://closure-library.googlecode.com/svn/docs/class_goog_storage_mechanism_HTML5SessionStorage.html)
;;
;; The extension supports the following calls:
;;
;;  * map-style lookup - `(:search-results session-storage "default value")`
;;  * `get` lookups
;;  * `(count session-storage)` - the number of things/keys stored
;;  * `(assoc! session-storage :new-key "saved")` - update or add an item
;;  * `(dissoc! session-storage :saved-results)` - remove an item
;;  * `(empty! session-storage)` - Clear out the localStorage store
;;
;;
;; Using sessionStorage in Pub/Sub
;; -----------------------------
;;
;; The apprpriate IWatchable support is attached to Google's HTML5SessionStorage
;; to allow it to participate in Shoreleave's pub/sub system
;;
;; To enable it, you need to `(publishable/include-sessionstorage!)` in the file
;; where you setup and wire together your bus and publishables.

(extend-type goog.storage.mechanism.HTML5SessionStorage

  IWatchable
  (-notify-watches [ss oldval newval]
    (doseq  [[key f] @ss-watchers]
      (f key ss oldval newval)))
  (-add-watch [ss key f]
    (swap! ss-watchers assoc key f))
  (-remove-watch [ss key]
    (swap! ss-watchers dissoc key)))


;; ###Usage
;; You'll typically do something like: `(def session-storage (sessionstorage/storage)`
(defn storage
  "Get the browser's sessionStorage"
  []
  (goog.storage.mechanism.HTML5SessionStorage.))

;;Much like how you can easily get "cookies/cookies" you can get "sessionstorage/sessionstorage"
(def sessionstorage (storage))

