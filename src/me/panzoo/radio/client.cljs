(ns me.panzoo.radio.client
  (:require
    [goog.events :as events]
    [goog.events.EventType :as event-type]
    [cljs.reader :as reader]))

(def ^:dynamic *rpc-url* "/rpc")
(def ^:dynamic *xhr-pool* nil)

(defn- cb-wrapper [cb evt]
  (let [xhr (. evt target)
        code (. xhr (getStatus))
        resp #(reader/read-string (. xhr (getResponseText)))]
    (cond
      (. xhr (isSuccess)) (cb :success (resp))
      (= 401 code) (cb :unauthorized nil)
      (= 501 code) (cb :unresolved nil)
      (= 500 code) (cb :server-error nil)
      :else (cb :error nil))))

(defn call [name args callback opts]
  (.getObject
    *xhr-pool*
    (fn [xhr]
      (when-let [ls (events/getListeners xhr event-type/COMPLETE false)]
        (doseq [l ls] (events/unlistenByKey (. l key))))
      (when callback
        (events/listenOnce
          xhr event-type/COMPLETE
          (partial cb-wrapper callback)))
      (when-let [ms (:timeout opts)]
        (.setTimeoutInterval xhr ms))
      (.send xhr @rpc-url "POST" (pr-str {:fname name :args args})))
    (:priority opts)))
