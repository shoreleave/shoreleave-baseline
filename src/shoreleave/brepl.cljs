(ns shoreleave.brepl
  (:require [clojure.browser.repl :as repl]))

(defn connect
  "Shorthand for the CLJS browser repl connect call"
  []
  (repl/connect "http://localhost:9000/repl"))

