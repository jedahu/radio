(ns radio.test
  (:use
    [radio.server :only (defrpc defrpc* rpc-handler)])
  (:require
    [cst.server :as cst]))

(defrpc* (constantly nil) test-divide [x y]
         (try (/ x y)
           (catch ArithmeticException e
             (throw
               (ex-info (. e getMessage) 
                        {:error-type :arithmetic
                         :msg (. e getMessage)})))))

(defrpc (constantly :not-admin) test-unauthorized [session]
  {:session session :body "This text should never be seen."})

(def rpch (rpc-handler {'test-divide #'test-divide
                        'test-unauthorized #'test-unauthorized}))

(defn handler [req]
  (if (= "/rpc" (:uri req))
    (rpch req) 
    {:status 404}))

(def rpc-server #(cst/serve-cljs % :handler handler))

(def repl-server #(cst/serve-brepl % :handler handler))
