(ns radio.test.client
  (:use
    [menodora.predicates :only (eq)])
  (:use-macros
    [menodora :only (defsuite describe should should* expect)])
  (:require
    [menodora.core :as mc]
    [radio.client :as rc]))

(defsuite rpcall-tests
  (describe "Content-Type"
    (should* "determine reader used"
      (rc/rpcall
        'hello-world []
        (fn [err data]
          (expect eq js/String (type data))
          (rc/rpcall
            'a-number []
            (fn [err data]
              (expect eq js/Number (type data))
              (rc/rpcall
                'a-string []
                (fn [err data]
                  (expect eq js/String (type data))
                  (rc/rpcall
                    'a-ol []
                    (fn [err data]
                      (expect eq LazySeq (type data))
                      (<done>))))))))))))

(defsuite chain-tests
  (describe "chain"
    :let [responses (atom [])]
    (should* "chain calls"
      (rc/chain
        ['hello-world [] #(do
                            (expect eq "Hello World!" %2)
                            (swap! responses conj %2))]
        ['a-number [] #(do
                         (expect eq 33 %2)
                         (swap! responses conj %2))]
        ['a-string [] #(do
                         (expect eq "thirty three" %2)
                         (swap! responses conj %2))]
        ['a-ol [] #(do
                     (expect eq "ol" (.. (first %2) -tagName (toLowerCase)))
                     (swap! responses conj (.. (first %2) -tagName (toLowerCase)))
                     (expect eq
                       ["Hello World!"
                        33
                        "thirty three"
                        "ol"]
                       @responses)
                     (<done>))]))))

(defn a-number 
  []
  (rc/rpcall 'a-number [] #(. js/console log (pr-str %&))))

(defn a-string 
  []
  (rc/rpcall 'a-string [] #(. js/console log (pr-str %&))))

(defn a-ol 
  []
  (rc/rpcall 'a-ol [] #(. js/console log (pr-str %&))))

(defn hello-world
  []
  (rc/rpcall 'hello-world [] #(. js/console log (pr-str %&))))

(defn hello-word
  []
  (rc/rpcall 'hello-world [] #(. js/console log %&)))

;;. vim: set lispwords+=defsuite,describe,should,should*,expect:
