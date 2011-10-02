(ns me.panzoo.radio.server)

(def ^:dynamic *session* {})

(defmacro defrpc [role name args & forms]
  (list* `defn (with-meta name (assoc (meta name) :role role))
         (vec args) forms))

(defn- permission-granted [fun user]
  (let [role (:role (meta fun))]
    (or (= :any role)
        (get (:roles user) role))))

(defn rpcall [req]
  (let [{:keys [fname args]} (read-string (ppr (slurp (:body req))))
        f (get (ns-publics 'pzcj.rpc) fname)
        session (:session req)]
    (cond
      (permission-granted f (:user session))
      (try
        (binding [*session* session]
          {:status 200
           :headers {"Content-Type" "application/clojure; charset=utf-8"}
           :session *session*
           :body (pr-str (apply f args))})
        (catch Throwable t 
          {:status 500}))
      (not f) {:status 501}
      :else {:status 401})))
