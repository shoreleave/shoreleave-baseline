(ns baseline.controllers.site
  "Web specific controllers")

;; These are controller actions for the main site.
;; They're wired up via Compojure routes in `baseline/routes.clj`

(defn index [session]
  (str "Hello.  Your session is: " session
       "</br><a href=\"/test\">Test Shoreleave</a>"))

(defn test-shoreleave []
  (slurp "resources/public/html/test.html"))

