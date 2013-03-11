(defproject shoreleave-baseline "0.3.0"
  :description "A baseline application to get started with a Compojure+Shoreleave"
  :url "https://github.com/shoreleave/shoreleave-baseline"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.7.0"]
                 [compojure "1.1.5" :exclusions [org.clojure/clojure]]
                 [lib-noir "0.4.7" :exclusions [[org.clojure/clojure]
                                                [compojure]
                                                [hiccup]
                                                [ring]]]
                 [ring "1.1.8"]
                 [ring-server "0.2.8" :exclusions [[org.clojure/clojure]
                                                   [ring]]]
                 [ring-refresh "0.1.2" :exclusions [[org.clojure/clojure]
                                                    [compojure]]]
                 [crypto-random "1.1.0"]
                 [amalloy/ring-gzip-middleware "0.1.2" :exclusions [org.clojure/clojure]]
                 [hiccup "1.0.2" :exclusions [org.clojure/clojure]]]
  :dev-dependencies [[lein-marginalia "0.7.1"]]
  :plugins [[lein-ring "0.8.3" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "0.3.0"]]
  :ring {:handler baseline.handler/war-handler
         :init baseline.handler/init
         :destroy baseline.handler/destroy}
   :cljsbuild {:builds [{:source-paths ["src"],
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
  ;
  ; -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis =50 -XX:+G1ParallelRSetUpdatingEnabled -XX:+G1ParallelRSetScanningEnabled
  ;  -XX:+AggressiveOpts -XX:CompileThreshold=500 -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseCompressedStrings -XX:+UseCompressedOops
)

