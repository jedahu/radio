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
  `session`. The definition can either return a map, which will be treated as a
  ring response, or a non-map value, which will be pr-str'd to the response map
  as the :body. In either case, if the :session key is missing, the session
  will stay the same.

  See radio.server.response for functions that wrap return values with the
  appropriate Content-Type headers.

  Use `defrpc*` if the function will not modify the session.

  Ensure the :body value is serializable using `pr-str` and that the
  serialization is readable by the clojurescript reader.

  To signal an error, either return a map with :status set to 500 or another
  error number, or throw an ex-info exception with the same map data (:status
  optional, default 500). If the error map contains a :session key the session
  will be updated, otherwise it will stay the same."
  [denied? name [session & args] & forms]
  (list* `defn
         (with-meta name (assoc (meta name) :denied? denied?))
         (apply vector session args)
         forms))

(defmacro defrpc*
  "The same as `defrpc` but without a session binding."
  [denied? name args & forms]
  `(defrpc ~denied? ~name [sess# ~@args] ~@forms))

(defn- rpc-response
  [f args current-session]
  (if-not f
    {:status 501
     :body :unresolved}
    (if-let [err-value ((:denied? (meta f)) current-session)]
      {:status 401
       :body [:unauthorized err-value]}
      (try
        (let [resp (apply f current-session args)]
          (if (map? resp)
            (merge {:status 200} resp)
            {:status 200 :body resp}))
        (catch clojure.lang.ExceptionInfo e
          (merge {:status 500} (ex-data e)))))))

(defn- rpc-handler-
  [fun-map req]
  (let [body (slurp (:body req))
        [fname & args] (read-string body)
        f (get fun-map fname)
        current-session (:session req)
        resp (rpc-response f args current-session)
        headers (:headers resp)]
    (assoc
      (merge
        {:session current-session}
        resp)
      :headers (merge
                 {"Content-Type"
                  "application/clojure; charset=utf-8"}
                 headers))))

(defn rpc-handler
  "Takes a map of symbols to functions and returns a ring handler which
  dispatches HTTP POST requests to functions in the map. Return values of
  RPC functions are serialized using `pr-str` and sent as the body of a HTTP
  response. Should there be an error, the codes 500, 501, and 401 are sent
  appropriately.

  All RPC functions could be defined in a single namespace, in which case the
  function map can be obtained by calling `ns-publics` on that namespace."
  [fun-map]
  (fn [req]
    (try
      (let [resp (rpc-handler- fun-map req)]
        (if (re-seq #"^application/clojure" (get-in resp [:headers "Content-Type"]))
          (update-in resp [:body] pr-str)
          resp))
      (catch Throwable t
        {:status 500
         :body (pr-str [:uncaught (. t getMessage)])}))))    

(def all (constantly nil))
