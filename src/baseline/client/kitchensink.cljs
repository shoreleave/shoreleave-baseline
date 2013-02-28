(ns baseline.client.kitchensink
  (:require [shoreleave.browser.storage.localstorage :as l-storage]))

;; These are all top-level functions that implement
;; the client's business logic - NOT THE PLUMBING - just the raw actions.
;;
;; You can think of this as a client side "controller"

;; Here is a atom'd hashmap - a great choice for storing state.
;; Unlike localStore, it's not attached to the DOM, so it's fast and easy to access.
;; If you look in `flows.cljs`, you'll see how atoms can participate in the Pub/Sub system.
(def quick-store (atom {:b 5}))

;; Let's keep track of one "special" value in localstorage.
;; We'll use this function to illustrate how to make calls to LS,
;; and also hide the implementation details from the rest of the system.
(defn update-ks-storage! [ks-value]
  (assoc! l-storage/localstorage :kitchen-sink ks-value))

(defn the-kitchen-sink []
  (:kitchen-sink l-storage/localstorage))

;; I'm using this just as an example to show how LS behaves like a map
(defn merge-storages [& extra-maps]
  (apply merge @quick-store l-storage/localstorage extra-maps))

