(ns me.panzoo.radio.client.test
  (:use
    [me.panzoo.radio.client :only (*xhr-pool* rpc-call)])
  (:require
    [goog.net.XhrIoPool :as xhrio-pool]
    [goog.dom :as dom]
    [goog.events :as events]
    [goog.events.EventType :as event-type]))

(defn run []
  (.log js/console "symbol" (keyword? (quote symbol)) (symbol? :keyword))
  (let [succ (dom/createDom "div" (.strobj {"id" "success"}))
        una (dom/createDom "div" (.strobj {"id" "unauthorized"}))
        unr (dom/createDom "div" (.strobj {"id" "unresolved"}))
        serr (dom/createDom "div" (.strobj {"id" "server-error"}))
        err (dom/createDom "div" (.strobj {"id" "error"}))
        callback (fn [node]
                   (fn [err arg]
                     (dom/setTextContent
                       node
                       (if err (pr-str err) (pr-str arg)))))]
    (dom/appendChild (. js/document body)
                     (dom/createDom "div" nil succ una unr serr err))
    (binding [*xhr-pool* (new (js* "goog.net.XhrIoPool") nil 1 2)]
      (rpc-call 'test-divide [9 3] (callback succ))
      (rpc-call 'test-unauthorized ['abc] (callback una))
      (rpc-call 'test-unresolved [] (callback unr))
      (rpc-call 'test-divide [9 0] (callback serr)))))
