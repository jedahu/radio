(defproject
  radio "0.1.0-SNAPSHOT"

  :description "RPC from clojurescript to clojure"

  :dependencies
  [[org.clojure/clojure "1.4.0"]]

  :dev-dependencies
  [[cst "0.1.0"]
   [menodora "0.1.0"]]

  :exclusions
  [org.apache.ant/ant]

  :plugins
  [[lein-cst "0.1.0"]]

  :cst
  {:builds
   {:dev {:output-dir ".cst-out/dev"
          :optimizations nil
          :pretty-print true}
    :small {:output-dir ".cst-out/small"
            :optimizations :simple
            :pretty-print true}
    :tiny {:output-dir ".cst-out/tiny"
           :optimizations :advanced
           :pretty-print false}}
   :build :dev
   :runners
   {:browser {:cljs menodora.runner.console/run-suites-browser
              :proc radio.test/rpc-server}}
   :runner :browser
   :servers
   {:browser radio.test/repl-server}
   :server :browser
   :repl-dir ".cst-repl"})
