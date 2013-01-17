(ns shoreleave.browser.cookies
  "An idiomatic interface to cookies"
  (:require [goog.net.Cookies :as gCookies]
            [goog.string :as gstr]))

;; Cookie support
;; --------------
;;
;; Shoreleave's cookie support is built upon [Closure's Cookies](http://closure-library.googlecode.com/svn/docs/class_goog_net_Cookies.html).
;;
;; The base object is extended to support the following calls:
;;
;;  * map-style lookup - `(:csrf-token cookies "default value")`
;;  * `get` lookups
;;  * seqable behavior - `(map identity cookies)`
;;  * `(count cookies)`
;;  * `(keys cookies)`
;;  * `(persistent! cookies)` - a PersistentHashMap of the cookies
;;  * `(assoc! cookies :new-key "saved")` - updating the cookies
;;  * `(dissoc! cookies :csrf-token)` - removing things from the cookies
;;  * `(empty! cookies)` - delete all cookies

(declare as-hash-map)

;; TODO: Consider making Cookies extend IWatchable

(extend-type goog.net.Cookies

  ILookup
  (-lookup
    ([c k]
      (-lookup c k nil))
    ([c k not-found] ;gstr/urlDecode
      (let [v (.get c (name k) not-found)]
        (if (string? v)
          (gstr/urlDecode v)
          v))
      #_(.get c (name k) not-found)))

  ISeqable
  (-seq [c]
    (map vector (.getKeys c) (.getValues c)))

  ICounted
  (-count  [c] (.getCount c))

  IFn
  (-invoke
    ([c k]
      (-lookup c k))
    ([c k not-found]
      (-lookup c k not-found))) 

  ITransientCollection
  (-persistent! [c] (as-hash-map c))
  ;(-conj! [c v] nil)

  ITransientAssociative
  (-assoc! [c k v & opts]
    (when-let [k (and (.isValidName c (name k)) (name k))]
      (let [{:keys [max-age path domain secure?]} (apply hash-map opts)]
        (.set c k v max-age path domain secure?))))

  ITransientMap
  (-dissoc! [c k & opts]
    (when-let [k (and (.isValidName c (name k)) (name k))]
      (let [{:keys [path domain]} (apply hash-map opts)]
        (.remove c k path domain))))

  IAssociative
  (-assoc [c k v]
    (-assoc (-persistent! c) k v))
  (-contains-key? [c k]
    (.containsKey c (name k)))

  ;IPrintable
  ;(-pr-seq  [c opts]
  ;  #_(let  [pr-pair  (fn  [keyval]  (pr-sequential pr-seq "" " " "" opts keyval))]
  ;    (pr-sequential pr-pair "{" ", " "}" opts c))
  ;  (-pr-seq (-persistent! c) opts))
  IPrintWithWriter
  (-pr-writer [c writer opts]
    (-write writer (-persistent! c)))

  ;; TODO: using the persistent version here might be a bad idea
  IHash
  (-hash [c]
    (-hash (-persistent! c))))

(def cookies (goog.net.Cookies. js/document))

(defn as-hash-map
  ([]
   (as-hash-map cookies))
  ([cks]
   (zipmap (.getKeys cks) (.getValues cks))))

(defn cookies-enabled?
  "Returns a boolean, true if cookies are currently enabled for the browser"
  ([]
   (cookies-enabled? cookies))
  ([cks]
   (.isEnabled cks)))
 
(defn empty! [cks]
  (.clear cks))
 
