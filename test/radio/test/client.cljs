(ns radio.test.client
  (:use
    [menodora.predicates :only (eq)])
  (:use-macros
    [menodora :only (defsuite describe should should* expect)])
  (:require
    [menodora.core :as mc]
    [radio.client :as rc]
    [radio.client.readers :as r]))

(defsuite reader-tests
  (describe "read-html"
    (should "return a seq"
      (expect eq LazySeq (type (r/read-html "abc"))))))

(defsuite rpcall-tests
  (describe "Content-Type"
    (should* "determine reader used"
      (rc/rpcall
        'hello-world []
        (fn [err data]
          (expect eq "Hello World!" data)
          (rc/rpcall
            'a-number []
            (fn [err data]
              (expect eq 33 data)
              (rc/rpcall
                'a-string []
                (fn [err data]
                  (expect eq "thirty three" data)
                  (rc/rpcall
                    'a-ol []
                    (fn [err data]
                      (expect eq LazySeq (type data))
                      (<done>))))))))))))

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
