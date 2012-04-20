;; # HTTP RPC client.
;;
;; The client needs two pieces of information, both of which are stored in atoms: 
;;
;; 1. The URL at which the server handles RPC POST requests.
;; 2. A goog.net.XhrManager to use to make the requests.
;;
;; There is a default URL. A default manager is instantiated if one does not exist
;; the first time a remote call is made. 
;;
(ns radio.client
  (:require
    [goog.events :as events]
    [goog.net.EventType :as event-type]
    [goog.net.XhrManager :as xhr-manager]
    [goog.net.XhrManager.Event :as xhr-event]
    [goog.net.ErrorCode :as err]
    [cljs.reader :as reader]))

(def rpc-url
  "The URL to which RPC posts are made."
  (atom "/rpc"))

(def xhr-manager
  "A goog.net.XhrManager."
  (atom nil))

(defn default-manager
  []
  (goog.net.XhrManager. 1 nil 1 10 0))

(defn wrap-callback 
  "Wrap the supplied callback to simplify parsing and error handling."
  [callback]
  (fn [evt]
    (.. js/document -body (appendChild (. js/document createTextNode "callback...")))
    (. js/console log "callback...")
    (. js/console log evt)
    (let [xhr (. evt -target)
          text (. xhr getResponseText)
          response #(reader/read-string text)]
      (try
        (cond
          (. xhr isSuccess)                        (callback nil (response))
          (= err/ABORT (. xhr getLastErrorCode))   (callback :client :abort)
          (= err/TIMEOUT (. xhr getLastErrorCode)) (callback :client :timeout)
          :else                                    (callback :server (response)))
        (catch js/Object e
          (callback :reader text))))))

(defn rpcall 
  "Call the remote function denoted by the `fname` symbol with `args` and on
  completion call `callback`. Return a goog.net.XhrManager.Request.

  `opts` is a map which may contain :priority and :max-retries keys of integer
  values. See documentation for goog.net.XhrManager for details. 

  `callback` must be a function of three arguments:
 
  error
      on success       nil
      on server error  :server 
      on client error  :client
      on read error    :reader

  data   
      on success       a clojure data structure read from the response body
      on server error  a clojure data structure read from the response body
      on client error  :abort or :timeout
      on read error    response text"
  ([fname args callback]
   (rpcall fname args nil callback))
  ([fname args opts callback]
   (swap! xhr-manager #(or % (default-manager)))
   (. @xhr-manager send
      (gensym)                    ; id
      @rpc-url                    ; url
      "POST"                      ; method
      (pr-str (list* fname args)) ; content
      nil                         ; headers
      (:priority opts)            ; priority
      (wrap-callback callback)    ; callback
      (:max-retries opts))))      ; max-retries
