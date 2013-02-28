(ns baseline.handler
 "The app-specific Ring handler" 
  (:require [compojure.handler :as handler]
            [baseline.routes :as routes]
            [baseline.config :refer [config]]
            [ring.middleware.gzip]
            [ring.middleware.file-info]
            [ring.middleware.anti-forgery]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [shoreleave.middleware.rpc]
            [hiccup.middleware]))

;; This is the main entry point into the application.
;; We wire up or middleware, attach our routes, and expose
;; the final application Handler.

;; This is the system initialization called by Ring.
;; It's often used to create files or otherwise prepare all system settings needed
(defn init []
  (println "The baseline app is starting"))

;; Like `init` this is the destroy Ring hook - it's used to cleanup
(defn destroy []
  (println "The baseline app has been shut down"))

(def app routes/all-routes)

;; Ring middleware are all Handler decorators.
;; That is, they take handlers, wrap around them, and return a new handler.
;; Here we build up all the middleware we need by threading our `app` (a top-level handler) through
(defn get-handler [app]
  (-> app
    (shoreleave.middleware.rpc/wrap-rpc)
    (ring.middleware.anti-forgery/wrap-anti-forgery)
    (ring.middleware.gzip/wrap-gzip)
    (handler/site {:session {:cookie-name "baseline"
                             :store (cookie-store {:key (config :session-secret)})
                             ;:store (cookie-store)
                             :cookie-attrs {:max-age (config :session-max-age-seconds)
                                            :http-only true}}})
    (ring.middleware.file-info/wrap-file-info)
    (hiccup.middleware/wrap-base-url)))

(def war-handler (get-handler app)) 

