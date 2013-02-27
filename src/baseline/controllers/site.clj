(ns baseline.controllers.site)

(defn index [session]
  ;(common/render "Hello World")
  (str "Hello.  Your session is: " session
       "</br><a href=\"/test\">Test Shoreleave</a>"))

(defn test-shoreleave []
  (slurp "resources/public/html/test.html"))

