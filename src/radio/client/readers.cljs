(ns radio.client.readers)

(defn read-html
  [text]
  (let [wrapper (. js/document createElement "div")]
    (set! (. wrapper -innerHTML) text)
    (let [cns (. wrapper -childNodes)]
      (for [i (range (. cns -length))]
        (aget cns i)))))
