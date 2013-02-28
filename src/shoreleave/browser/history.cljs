(ns shoreleave.browser.history
  "An idiomatic interface to browser history"
  (:require [goog.events :as gevents]
            [goog.History :as ghistory]
            [goog.history.EventType :as history-event]
            [goog.history.Html5History :as history5]))

;; This is the history object - the interface the browser's history
;;
;; You can initialize your own history object, but you're encouraged to
;; us the one provided
(declare history)

;; Navigation Events
;; -----------------
;;
;; Adding History support to your application allows you to
;; correctly handle location-bar state and enables correct usage
;; of a browser's navigation buttons (like the back button).
;;
;; Every point of "navigation" within your app can be added
;; to the browsers history, allowing you to go backwards and
;; forwards in that history
;;
;; History events are packaged up as a map with the keys:
;; `:token :type :navigation?`
;;
;; `:token` is the location URL associated with this point in history.
;; `:type` is the type of history event that was captured. You can ignore this.
;; `:navigation?` is a boolean, True if the event was initiated by a browser,
;; or false otherwise

(defn navigate-callback
  "Add a function to be called when a navigation event happens.
  The function should accept a single map, with keys :token, :type, :navigation?"
  ([callback-fn]
   (navigate-callback history callback-fn))
  ([hist callback-fn]
   (gevents/listen hist history-event/NAVIGATE
                  (fn [e]
                    (callback-fn {:token (keyword (.-token e))
                                  :type (.-type e)
                                  :navigation? (.-isNavigation e)})))))

(defn init-history
  "Initialize the browser's history, with HTML5 API support if available"
  []
  (let [history (if (history5/isSupported)
                  (goog.history.Html5History.)
                  (goog.History.))]
                   (.setEnabled history true)
                   (gevents/unlisten (.-window_ history) (.-POPSTATE gevents/EventType) ; This is a patch-hack to ignore double events
                                     (.-onHistoryEvent_ history), false, history)
                   history))

(def history (init-history))
(defn get-token
  "Get the current token/url string in the browser history"
  [hist]
  (.getToken hist))

(defn set-token!
  "Add a new token to the brower's history.  This will trigger a nvaigate event"
  [hist tok]
  (.setToken hist tok))

(defn replace-token! [hist tok] (.replaceToken hist tok))

;; Raw access to the HTML5 History API
;; -----------------------------------
;;
;; This is advantageous when you want to use the stateobj
;; for partial view or data caching
;;
;; For most applications this is not needed (nor is it advised).
;; It's useful for storing remote URLs the page needed, or small
;; pieces of page specific state that need to be restored for the page to
;; be functional.

(defn push-state [hist state-map]
  (let [{:keys [state title url]
         :or {state nil
              title js/document.title}} state-map]
    (apply js/window.history.pushState (map clj->js [state title url]))
    (.dispatchEvent hist (goog.history.Event. url false))))

