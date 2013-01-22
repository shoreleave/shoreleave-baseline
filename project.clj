(defproject shoreleave-baseline "0.3.0"
  :description "A baseline application to get started with Compojure+Shoreleave"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.0-RC1"]
                 [compojure "1.1.5" :exclusions [org.clojure/clojure]]
                 [lib-noir "0.3.4" :exclusions [[org.clojure/clojure]
                                                [compojure]
                                                [hiccup]
                                                [ring]]]
                 [ring "1.1.7"]
                 [ring-server "0.2.5" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring-refresh "0.1.1" :exclusions [[org.clojure/clojure]
                                                    [compojure]]]
                 [crypto-random "1.1.0"]
                 [amalloy/ring-gzip-middleware "0.1.2" :exclusions [org.clojure/clojure]]
                 [hiccup "1.0.2" :exclusions [org.clojure/clojure]]]
  :dev-dependencies [[lein-marginalia "0.7.1"]]
  :plugins [[lein-ring "0.8.0" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "0.2.10"]]
  :ring {:handler baseline.handler/war-handler
         :init baseline.handler/init
         :destroy baseline.handler/destroy}
   :cljsbuild {:builds [{:source-path "src",
                        :compiler {:output-dir "resources/build/cljs",
                                   :output-to "resources/public/js/baseline.js",
                                   ;:externs  ["externs/jquery.js"],
                                   :optimizations :simple,;:advanced ;:whitespace
                                   :pretty-print true}}]}
  :profiles {:production 
             {:ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}}
  
  :warn-on-reflection false
  ;:run-aliases {}
  :main baseline.server

  ; Different JVM options for performance
  ;:jvm-opts ["-Xmx1g"]
  ;:jvm-opts ["-server" "-XX:+UseConcMarkSweepGC" "-XX:+UseParNewGC" "-XX:+UseCompressedOops"]
  ;:jvm-opts ["-server" "-Xmx1g" "-XX:+UseConcMarkSweepGC" "-XX:+UseParNewGC" "-XX:+UseCompressedOops"]
  ;:jvm-opts ["-server" "-Xmx50mb" "-XX:+UseConcMarkSweepGC" "-XX:+UseParNewGC" "-XX:+UseCompressedOops"]
)

