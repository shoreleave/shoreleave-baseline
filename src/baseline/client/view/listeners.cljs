(ns baseline.client.views.listeners
  (:require [clojure.browser.dom :as c-dom]
            [clojure.browser.event :as c-event]))

;; We use this file to attach listeners to items of interest in the DOM
;; Placing this all in a single file keeps it out of our HTML,
;; hides the details from the rest of the system,
;; and allows `flows.cljs` to feel a little more declarative

;; Usually you'd want to use something like Enfocus or Domina
;; to do this.
;; I'm using the Closure Library directly to keep the dependencies out.

;; Attach a function `f`, that accepts a single argument (the event object/info),
;; to the element with the id, `mainbutton`
;; The function will be called for every "click" event
(defn attach-mainbutton [f]
  (c-event/listen (c-dom/get-element "mainbutton") 
                  "click" #(f %))) ;; this turns our "f" into a callback

