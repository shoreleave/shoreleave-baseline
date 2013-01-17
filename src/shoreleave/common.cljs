(ns shoreleave.common
  "Shoreleave's common library provides generic auxiliary functions
  that are used throughout the shoreleave utilites."
  (:require [shoreleave.brepl :as brepl]))

;; Data shaping
;; ------------
;;
;; Below are functions commonly used for shaping the data being passed around
;; your client-side application.  This is most commonly used when you need to
;; communicate with a server (your own or a remote one via CORS/JSONP call),
;; or when you're interopating with other JavaScript libraries.
;;
;; These calls should be used sparingly, as they do depend on CLJS implementation
;; details that could change.

;; `clj->js` is now a proper protocol in CLJS core.

;; Location bar manipulation
;; -------------------------
;;
;; Most apps require checking for or modifying the location bar.
;; Shoreleave supports converting location-bar data into maps,
;; making them easier to incorporate into your applications.

(defn args-map
  "Take a location-bar string and build a map of the arguments structure found in it.
  This will work for hash-string and query-string.
  Please note you should not be using hash-bang (#!) URLs anymore - HTML5 history API
  allows you to change the location URL without a refresh."
  [location-str]
  (let [query-args-obj (goog.Uri.QueryData. (if (contains? #{\# \?} (get location-str 0))
                                              (subs location-str 1)
                                              location-str))]
    (zipmap (map keyword (.getKeys query-args-obj)) (.getValues query-args-obj))))

;; *TIP:* The hash-string is great for in-view query args.
;; If you need to update the location without a page refresh, please see
;; [Shoreleave's history API](https://github.com/shoreleave/shoreleave-browser/blob/master/src/shoreleave/browser/history.cljs)


(defn query-args-map
  "Return a  map of the query-string arguments"
  []
  (args-map js/window.location.search))
(defn hash-args-map
  "Return a map of the hash-string arguments"
  []
  (args-map js/window.location.hash))

(defn set-window-hash-args
  "Given a map, set the hash-string arguments; Returns the map"
  [args-map]
  (let [hash-str (reduce (fn [old-str [k v]] (str old-str (name k) "=" v "&")) "#" args-map)
        clean-hash-str (subs hash-str 0 (dec (count hash-str)))]
    (set! js/window.location.hash clean-hash-str)
    args-map))

;; Developer Support
;; ----------------
;;

(defn toggle-brepl
  "Toggle the browser REPL automatically.
  With no arguments, it'll connect the REPL if 'brepl' is a query-string argument.
  You can optionally pass in your own query-key and query-map; for example
  if you want to use hash-string arguments instead"
  ([]
    (toggle-brepl (query-args-map)))
  ([query-map]
    (toggle-brepl query-map :brepl))
  ([query-map query-key]
    (when (query-map query-key)
      (brepl/connect))))

