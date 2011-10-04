(ns me.panzoo.radio.test
  (:use
    me.panzoo.radio.server
    clojure.test
    clj-webdriver.core
    ring.adapter.jetty
    [cljs.closure :only (build)]))

(defrpc :any test-divide [x y]
  (/ x y))

(defrpc :authd test-unauthorized []
  "This text should never be seen.")

(def rpch (rpc-handler {'test-divide #'test-divide
                              'test-unauthorized #'test-unauthorized}))

(defn handler [req]
  (cond
    (= "/rpc" (:uri req)) (rpch req)
    (re-seq #"^/out/" (:uri req)) {:status 200 :body (slurp (str "." (:uri req)))}
    :else
    {:status 200
     :body (str "<!DOCTYPE html>
                <html><head>
                <meta charset='UTF-8'>
                <script src='/out/testall.js'></script>"
                "</head>
                <body>
                <script>me.panzoo.radio.client.test.run()</script>
                </body>
                </html>")}))

(deftest radio
  (let [jetty (run-jetty handler {:port 9876 :join? false})
        b (do (Thread/sleep 500)
              (start :firefox "http://localhost:9876"))]
    (try
      (is
        (= "3" (text (find-it b {:id "success"})))
        "RPC not working.")

      (is
        (= ":unauthorized" (text (find-it b {:id "unauthorized"})))
        "Expected unauthorized error.")

      (is
        (= ":unresolved" (text (find-it b {:id "unresolved"})))
        "Expected unresolved error.")

      (is
        (= ":server-error" (text (find-it b {:id "server-error"})))
        "Expected server error.")

      (finally
        (close b)
        (.stop jetty)))))
