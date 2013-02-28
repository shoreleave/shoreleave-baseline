(ns baseline.controllers.api
  "An example API controller")

;; Building APIs for AJAX
;; ----------------------
;;
;; When using Shoreleave remotes, it's easiest to capture your API
;; as a collection of namespaced functions - like you would any library
;; or interface.  This encourages you to focus on the raw data and system
;; interactions, without litering plumbing/routing logic throughout the API.
;;
;; You can expose this namespace to the remoting system with Ring middleware.

;; Here, we're just creating a basic function that echos back a string
(defn ping-the-api [pingback]
  (str "You have hit the API with: " pingback))

