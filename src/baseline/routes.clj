(ns baseline.routes
  (:require [compojure.core :as c-core :refer [defroutes
                                               GET POST PUT DELETE
                                               HEAD OPTIONS PATCH
                                               ANY]]
            [compojure.route :as c-route]
            [shoreleave.middleware.rpc :refer [remote-ns]]
            ;; Controllers
            [baseline.controllers.site :as cont-site]
            ;; Public APIs
            [baseline.controllers.api]))

;; Remote APIs exposed
;; -------------------
(remote-ns 'baseline.controllers.api :as "api")

;; Controller routes, ROA oriented
;; -------------------------------
(defroutes site
  (GET "/" {session :session} (cont-site/index session))
  (GET "/test" [] (cont-site/test-shoreleave)))

;; Core system routes
;; ------------------
(defroutes app-routes
  (c-route/resources "/")
  (c-route/not-found "404 Page not found."))

;; The top-level collection of all routes
;; --------------------------------------
(def all-routes (c-core/routes site
                               app-routes))

