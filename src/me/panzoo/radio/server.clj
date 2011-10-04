(ns me.panzoo.radio.server
  "# HTTP RPC server

  This namespace contains a function for defining remote callable functions and
  a function for creating a ring handler to dispatch well-formed HTTP POST
  requests to said functions. The handler binds the ring session map to a
  dynamic variable for the duration of the RPC and subsequently sets the
  session map to the variable's (potentially) changed value.")

(def ^:dynamic *session*
  "Binds the ring session map for the duration of an RPC."
  {})

(defmacro defrpc
  "Define a function with a keyword `role`. The RPC handler ensures only
  requests with a `:user` containing a `:roles` set containing the keyword
  `role` will be able to call the defined function. Pass `:any` as the role
  to allow any RPC request.
  
  Within the body of this definition, the ring session map is bound to
  `*session*`. To change the session in the HTTP response, use `set!`.
  
  Ensure the return value of the defined function is serializable using
  `pr-str` and readable by the clojurescript reader."
  [role name args & forms]
  (list* `defn (with-meta name (assoc (meta name) :role role))
         (vec args) forms))

(defn- permission-granted?
  "Returns true if the role associated with the function `fun` matches one of
  the roles in the `user` map's `:roles`, or if the role associated with `fun`
  is `:any`."
  [fun user]
  (let [role (:role (meta fun))]
    (or (= :any role)
        (get (:roles user) role))))

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
    (let [{:keys [fname args]} (read-string (slurp (:body req)))
          f (get fun-map fname)
          session (:session req)]
      (cond
        (permission-granted? f (:user session))
        (try
          (binding [*session* session]
            {:status 200
             :headers {"Content-Type" "application/clojure; charset=utf-8"}
             :session *session*
             :body (pr-str (apply f args))})
          (catch Throwable t 
            {:status 500}))
        (not f) {:status 501}
        :else {:status 401}))))
