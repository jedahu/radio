(ns me.panzoo.radio.client.test
  (:use
    [me.panzoo.radio.client :only (*xhr-pool* rpc-call)])
  (:require
    [goog.net.XhrIoPool :as xhrio-pool]
    [goog.dom :as dom]
    [goog.events :as events]
    [goog.events.EventType :as event-type]
    [clojure.browser.repl :as repl]))

(defsuite phantom-tests
  (describe "successful call"
    :let [result (atom nil)]
    (should "return"
      (rpcall 'test-divide [9 3] #(reset! result [%1 %2]))
      (expect

(defn ^:export run []
  (repl/connect "http://localhost:9000/repl"))

  #_((.log js/console "symbol" (keyword? (quote symbol)) (symbol? :keyword))
  (let [succ (dom/createDom "div" (.strobj {"id" "success"}))
        una (dom/createDom "div" (.strobj {"id" "unauthorized"}))
        unr (dom/createDom "div" (.strobj {"id" "unresolved"}))
        serr (dom/createDom "div" (.strobj {"id" "server-error"}))
        callback (fn [node]
                   (fn [err arg]
                     (dom/setTextContent
                       node
                       (if err (pr-str err) (pr-str arg)))))]
    (dom/appendChild (. js/document body)
                     (dom/createDom "div" nil succ una unr serr))
    (binding [*xhr-pool* (new (js* "goog.net.XhrIoPool") nil 1 2)]
      (rpc-call 'test-divide [9 3] (callback succ))
      (rpc-call 'test-unauthorized ['abc] (callback una))
      (rpc-call 'test-unresolved [] (callback unr))
      (rpc-call 'test-divide [9 0] (callback serr)))))

;;. vim: set lispwords+=defsuite,describe,should,expect:
