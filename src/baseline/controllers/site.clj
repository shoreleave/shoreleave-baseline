(ns baseline.controllers.site)

(defn index [session]
  ;(common/render "Hello World")
  (str "Hello.  Your session is: " session)
  )

(defn test-shoreleave []
  (slurp "resources/public/html/test.html"))
