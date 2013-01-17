(ns baseline.config
  (:require [shoreleave.server-helpers :refer [safe-read]]))

(defn read-config
  ""
  ([]
   (read-config "resources/config.edn"))
  ([config-loc]
   (safe-read (slurp config-loc))))

(def config (read-config))

