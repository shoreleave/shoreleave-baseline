(ns shoreleave.browser.blob
  "An idiomatic interface to Blobs")

;; Blobs
;; -----
;;
;; HTML5 File API supports the creation of Blobs.
;;
;; Blobs allow you to take arbitrary text (like functions) and create file-like
;; objects, that get their own unique URL wuth a `blob://...` schema.
;;
;; This is useful if you're making an app that wants to use on-demand assets,
;; or you need to build something like embedded web workers.
;; It also comes in handy if you want to build dynamic content on the fly,
;; like streaming images.
;;
;; The [Blob API](https://developer.mozilla.org/en-US/docs/DOM/Blob) is now a stable spec in HTML5.
;;
;; To create blobs, you pass a vector of file contents (or parts) to `(blob ...)`
;; Optionally, you can set the content-type of the blob by passing in the content-type string.
;; For example, "text\/xml"

(defn- window-url-prop
  []
  (or (.-URL js/window) (.-webkitURL js/window)))

(defn raw-blob
  "Build a new Blob object, but don't muck with the args.
  This is for low-level interop stuff - when needed."
  ([file-parts]
   (js/Blob. file-parts))
  ([file-parts prop-bag]
   (js/Blob. file-parts prop-bag)))

(defn blob
  "Build the file-contents into a Blob and return it.
  Optionally set the content-type via a string"
  ([file-parts]
   (js/Blob. (clj->js file-parts)))
  ([file-parts content-type-str]
   (js/Blob. (clj->js file-parts) (js-obj "type" content-type-str))))

(defn object-url!
  "Create a unique object URL (ala `blob://...`) for a Blob object,
  as returned from `(blob ...)`"
  [file-or-blob]
  (let [url (window-url-prop)]
    (when url
      (.createObjectURL url file-or-blob))))

(defn revoke-object-url!
  ""
  [obj-url]
  (let [url (window-url-prop)]
    (when url
      (.revokeObjectURL url obj-url))))

