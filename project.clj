(defproject omnitech "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8"]
                 [com.andrewmcveigh/cljs-time "0.3.5"]
                 [garden "1.2.5"]]

  :plugins [[lein-cljsbuild "1.0.6-SNAPSHOT"]
            [lein-figwheel "0.3.1"]
            [refactor-nrepl "1.1.0-SNAPSHOT"]
            [cider/cider-nrepl "0.9.0-SNAPSHOT"]
            [lein-garden "0.2.6"]
            [lein-pdo "0.1.1"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/public/css/compiled" "target"]

  :garden {:builds [{:id "dev"
                     :source-paths ["src/clj/omnitech_styles"]
                     :stylesheet clj.omnitech-styles.core/omnitech
                     :compiler {:output-to "resources/public/css/compiled/omnitech.css"
                                :pretty-print? true}}
                    {:id "min"
                     :source-paths ["src/clj/omnitech_styles"]
                     :stylesheet clj.omnitech-styles.core/omnitech
                     :compiler {:output-to "resources/public/css/compiled/omnitech.css"
                                :pretty-print? false}}]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]

                        :figwheel {:on-jsload "cljs.omnitech.core/on-js-reload"
                                   :websocket-host "localhost"}

                        :compiler {:main cljs.omnitech.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/omnitech.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :optimizations :none
                                   :source-map true
                                   :source-map-timestamp true
                                   :cache-analysis true }}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/omnitech.js"
                                   :main cljs.omnitech.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}

  :aliases {"start-dev" ["do" "clean" ["pdo" ["garden" "auto" "dev"] "figwheel"]]
            "build-min" ["do" "clean" ["cljsbuild" "once" "min"] ["garden" "once" "min"]]})
