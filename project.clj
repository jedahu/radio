(defproject
  radio "0.1.0-SNAPSHOT"

  :description "RPC from clojurescript to clojure"

  :dependencies
  [[org.clojure/clojure "1.4.0"]]

  :dev-dependencies
  [[cst "0.2.1"]
   [menodora "0.1.1"]]

  :exclusions
  [org.apache.ant/ant]

  :plugins
  [[lein-cst "0.2.1"]]

  :cst
  {:suites [radio.test.client/reader-tests
            radio.test.client/rpcall-tests]
   :runners
   {:console-browser {:cljs menodora.runner.console/run-suites-browser
                      :proc radio.test.server/rpc-server}}
   :runner :console-browser})
