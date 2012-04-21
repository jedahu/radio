(ns radio.test.server
  (:use
    [radio.server :only (defrpc defrpc* rpc-handler all)])
  (:require
    [radio.server.response :as resp]
    [cst.server :as cst]))



(defrpc* all hello-world []
  []
  (resp/text "Hello World!"))

(defrpc* all a-number
  []
  33)

(defrpc* all a-string
  []
  (resp/clj "thirty three"))

(defrpc* all a-ol
  []
  (resp/html "<ol><li>one</li><li>two</li></ol>"))

(defrpc* all divide
  [x y]
  (try (/ x y)
    (catch ArithmeticException e
      (throw
        (ex-info (. e getMessage)
                 {:error-type :arithmetic
                  :msg (. e getMessage)})))))

(defrpc (constantly :not-admin) test-unauthorized
  [session]
  {:session session :body "This text should never be seen."})

(def rpch (rpc-handler {'hello-world #'hello-world
                        'a-number #'a-number
                        'a-string #'a-string
                        'a-ol #'a-ol
                        'divide #'divide
                        'test-unauthorized #'test-unauthorized}))

(defn handler [req]
  (if (= "/rpc" (:uri req))
    (rpch req)
    {:status 404}))

(def rpc-server #(cst/serve-cljs % :handler #'handler))

(def repl-server #(cst/serve-brepl % :handler #'handler))

;;. vim: set lispwords+=defrpc,defrpc*:
