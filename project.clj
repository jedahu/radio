(defproject
  radio "0.1.0-SNAPSHOT"

  :description "RPC from clojurescript to clojure"

  :dependencies
  [[org.clojure/clojure "1.4.0"]]

  :dev-dependencies
  [[cst "0.2.3"]
   [menodora "0.1.3"]]

  :exclusions
  [org.apache.ant/ant]

  :plugins
  [[lein-cst "0.2.3"]]

  :cst
  {:suites [radio.test.client/rpcall-tests
            radio.test.client/chain-tests]
   :runners
   {:console-browser {:cljs menodora.runner.console/run-suites-browser
                      :proc radio.test.server/rpc-server}
    :console-phantom {:cljs menodora.runner.console/run-suites-browser
                      :proc radio.test.server/rpc-server
                      :browser :phantom} }
   :runner :console-phantom})
