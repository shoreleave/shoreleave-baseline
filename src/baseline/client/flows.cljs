(ns baseline.client.flows
  (:require [shoreleave.pubsubs.simple :as pbus]
            [shoreleave.pubsubs.protocols :as pubsub]
            [shoreleave.pubsubs.publishable :as pcore]
            ;; Our functionality
            [baseline.client.kitchensink :as b-kitchensink]
            ;; Our listeners
            [baseline.client.views.listeners :as b-listeners])
  (:require-macros  [shoreleave.remotes.macros :as srm]))

;; This is where we wire the information flow for our application
;; You can think of this like routing, but for the client side app.
;;
;; Using pubsub, functions, atoms, storages - *anything* can
;; publish and subscribe to each other.
;; For example, the return values of functions are published to
;; the bus and passed along to any of the subscribers.
;;
;; Here's diagram of what's happening below:
;;
;; Button Click
;;  \---> Fn returning a map
;;         \---> Updating local storage with the new map
;;          \      \---> Sending a message back to the server ---> Echoing the response to the console
;;           \      \                                               \---> popping up an alert
;;            \      \---> Echoing LS to the console                 \---> echo the value of :kitchen-sink from ls
;;             \---> Print out to console

(def bus (pbus/bus))
(pcore/include-localstorage!)

(def ps-simple-echo (pubsub/publishize #(do (js/console.log "Simple pubsub echo:" %)
                                          %) bus))
(pubsub/subscribe bus ps-simple-echo #(js/alert "Simple Echo got hit with:" %))
(pubsub/subscribe bus ps-simple-echo #(js/console.log "And a kitchen sink holds:" (b-kitchensink/the-kitchen-sink)))

(def ps-update-ksls! (pubsub/publishize b-kitchensink/update-ks-storage! bus))
(pubsub/subscribe bus ps-update-ksls! #(do (srm/rpc (api/ping-the-api "PUBSUB ->") [pong-response] (ps-simple-echo pong-response))
                                         (js/console.log "localStorage is now:" %)))

(def ps-handle-mainbutton (pubsub/publishize (fn [e] {:important-thing 1}) bus))
(pubsub/subscribe bus ps-handle-mainbutton #(js/console.log "The button was clicked, the map is:" %))
(pubsub/subscribe bus ps-handle-mainbutton #(ps-update-ksls! (b-kitchensink/merge-storages %)))

(b-listeners/attach-mainbutton ps-handle-mainbutton)

