;; # HTTP RPC client.
;;
;; The client needs two pieces of information, both of which are bound in
;; dynamic variables:
;;
;; 1. The URL at which the server handles RPC POST requests.
;; 2. A goog.net.XhrIoPool to use to make the requests.
;;
;; There is a default URL but no default pool. Both must be bound before a RPC
;; is made.
;;
(ns me.panzoo.radio.client
  (:require
    [goog.events :as events]
    [goog.net.EventType :as event-type]
    [cljs.reader :as reader]))

(def ^:dynamic *rpc-url*
  "The URL to which RPC posts are made."
  "/rpc")

(def ^:dynamic *xhr-pool*
  "A goog.net.XhrIoPool."
  nil)

(defn- cb-wrapper
  "Wrap the supplied callback `cb` with its controlling XhrIoPool and XhrIo
  objects to achieve the following:
  
  1. Release of the XhrIo instance back to the pool after `cb` is called.
  2. Conversion of HTTP codes into the values mentioned in the `rpc-call`
     documentation.
  3. Reading of the response text using the clojurescript reader.
  
  The results of #2 and #3 are the arguments `cb` is called with."
  [pool xhr cb evt]
  (let [xhr (. evt target)
        code (. xhr (getStatus))
        resp #(reader/read-string (. xhr (getResponseText)))]
    (cond
      (. xhr (isSuccess)) (cb nil (resp))
      (= 401 code) (cb :unauthorized nil)
      (= 501 code) (cb :unresolved nil)
      (= 500 code) (cb :server-error nil)
      :else (cb :error nil)))
  (.releaseObject pool xhr))

(defn rpc-call
  "Call the remote function denoted by the `name` symbol with the arguments in
  the `args` vector, and on completion call `callback`.

  `callback` must be a function of two arguments. The first is `nil` on success
  and otherwise one of `:unauthorized`, `:unresolved`, `:server-error`, or
  `:error`, which correspond to the response codes 401, 501, 500, and any other
  error code. The second is a clojure data structure read in from the text of
  the HTTP response.

  `opts` is a map which may contain a `:priority` key with an integer value.
  This value is passed on as the priority argument to
  goog.net.XhrIoPool#getObject()."
  [name args callback & {:as opts}]
  (let [pool *xhr-pool*]
    (.getObject
      pool
      (fn [xhr]
        (when-let [ls (events/getListeners xhr event-type/COMPLETE false)]
          (doseq [l ls] (events/unlistenByKey (. l key))))
        (when callback
          (events/listenOnce
            xhr event-type/COMPLETE
            (partial cb-wrapper pool xhr callback)))
        (when-let [ms (:timeout opts)]
          (.setTimeoutInterval xhr ms))
        (.send xhr *rpc-url* "POST" (pr-str {:fname name :args args})))
      (:priority opts))))
