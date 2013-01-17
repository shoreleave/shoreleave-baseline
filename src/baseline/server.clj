(ns baseline.server
  (:require [baseline.config :refer [config]]
            [baseline.handler :as handler]
            [ring.server.standalone :as ring-server]))

;; You'll want to do something like: `(defonce server (start-server))`

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (or (and port (Integer/parseInt port))
                 (Integer. (get (System/getenv) "PORT" (config :baseline-port)))
                 8080)
        server (ring-server/serve (handler/get-handler #'handler/app) 
                                 {:port port 
                                  :init handler/init
                                  :auto-reload? true
                                  :destroy handler/destroy 
                                  :join true})]
    (println (str "You can view the site at http://localhost:" port))
    server))

(defn stop-server [server]
  (when server
    (.stop server)
    server))

(defn restart-server [server]
  (when server
    (doto server
      (.stop)
      (.start))))

(def server-starters {:dev start-server})

(defn -main [& m]
  (let [mode-kw (keyword (or (first m) :dev))
        server-fn (server-starters mode-kw)
        server (server-fn)]
    server))

