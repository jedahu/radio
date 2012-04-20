(ns radio.server
  "# HTTP RPC server

  This namespace contains a function for defining remote callable functions and
  a function for creating a ring handler to dispatch well-formed HTTP POST
  requests to said functions.")

(defmacro defrpc
  "Define a function with a predicate `denied?`, which takes the session as its
  only argument and returns a non-nil value if the user is not authorized to
  call the function. 

  Within the body of this definition, the ring session map is bound to
  `session`. The definition must return a map with :session, :body, and
  :status (optional) keys. Use `defrpc*` to return a simple value.
  
  Ensure the :body value is serializable using `pr-str` and that the
  serialization is readable by the clojurescript reader."
  [denied? name [session & args] & forms]
  (list* `defn
         (with-meta name (assoc (meta name) :denied? denied?))
         (apply vector session args)
         forms))

(defmacro defrpc*
  "The same as `defrpc` but without a session binding."
  [denied? name args & forms]
  `(defrpc ~denied? ~name [sess# ~@args]
           {:session sess#
            :body (do ~@forms)}))

(defn- rpc-handler-
  [fun-map req]
  (let [[fname & args] (read-string (slurp (:body req)))
        f (get fun-map fname)
        sess (:session req)]
    (if-not f
      {:status 501
       :body :unresolved}
      (if-let [err-value ((:denied? (meta f)) sess)]
        {:status 401
         :body [:unauthorized err-value]}
        (try
          (let [{:keys [session body status]} (apply f sess args)]
            {:status (or status 200)
             :headers {"Content-Type" "application/clojure; charset=utf-8"}
             :session session
             :body body})
          (catch Throwable t 
            {:status 500
             :body [:uncaught (ex-data t)]}))))))

(defn rpc-handler
  "Takes a map of symbols to functions and returns a ring handler which
  dispatches HTTP POST requests to functions in the map. Return values of
  RPC functions are serialized using `pr-str` and sent as the body of a HTTP
  response. Should there be an error, the codes 500, 501, and 401 are sent
  appropriately.

  All RPC functions could be defined in a single namespace, in which case the
  function map can be obtained by calling `ns-publics` on that namespace."
  [fun-map]
  (fn [req] (update-in (rpc-handler- fun-map req) [:body] pr-str)))
