(ns radio.server.response)

(defn- wrap-value
  [content-type]
  (fn [val & opts]
    (apply hash-map
           :body val
           :headers (merge {"Content-Type" content-type}
                           (:headers opts))
           (dissoc opts :headers))))

(def text (wrap-value "text/plain"))

(def html (wrap-value "text/html"))

(defn clj
  [val & opts]
  (apply hash-map :body val opts))
