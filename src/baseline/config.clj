(ns baseline.config
  "A *simple* config system."
  (:require [shoreleave.server-helpers :refer [safe-read]]))

;; Baseline Config
;; ---------------
;;
;; This is an MVC - Minimal Viable Config.
;; For a more advanced config system you, you could replace this with
;; [carica](https://github.com/sonian/carica) or [environ](https://github.com/weavejester/environ)

(defn read-config
  "Read a config file and return it as Clojure Data.
  Usually, this is a hashmap"
  ([]
   (read-config "resources/config.edn"))
  ([config-loc]
   (safe-read (slurp config-loc))))

(def config (read-config))

