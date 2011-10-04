# Radio

HTTP RPC server and client. Serializes argument and return values using
`pr-str` and the clojurescript reader. Provides role-based authorization using
the ring session map. More information in code doc-strings and comments.

## Synopsis

Server functions:

    (ns rpc.functions
      (:use me.panzoo.radio.server))

    (defrpc :any hello [a b]
      (str "Hello " a ", and " b "."))

    (defrpc :admin secret []
      {:secret ["data" 1]})


Server:

    (ns rpc.server
      (:use
        me.panzoo.radio.server
        ring.adapter.jetty))

    (defn handler [req]
      (if (= "/rpc" (:uri req))
        (rpc-handler (ns-publics 'rpc.functions))
        (handle other requests)))

    (run-jetty handler :port 3000)


Client:

    (ns rpc.client
      (:use
        [me.panzoo.radio.client :only (*rpc-url* *xhr-pool*)])
      (:require-macros
        [me.panzoo.radio.client.macro :as radio]))

    (defn callback [err ret]
      (.log js/console
            (if err
              (pr-str "error " err)
              ret)))

    (radio/call (hello "World" "Mum") callback)

    (radio/call (hello "World") callback)

    (radio/call (secret) callback)

    (radio/call (non-existent 1 2) callback)


Console:

    "\"Hello World, and Mum\"" ;; 200

    "error :server-error"      ;; 500

    "error :unauthorized"      ;; 401

    "error :unresolved"        ;; 501
