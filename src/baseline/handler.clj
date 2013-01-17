(ns baseline.handler 
  (:require [compojure.handler :as handler]
            [baseline.routes :as routes]
            [baseline.config :refer [config]]
            [ring.middleware.gzip]
            [ring.middleware.file-info]
            [ring.middleware.anti-forgery]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [shoreleave.middleware.rpc]
            [hiccup.middleware]))

(defn init []
  (println "The baseline app is starting"))

(defn destroy []
  (println "The baseline app has been shut down"))

(def app routes/all-routes)

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

