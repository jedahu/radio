(ns me.panzoo.radio.client.macro)

(defmacro rpc-call
  [[name & args] & [callback & [opts]]]
  `(me.panzoo.radio.client/call '~name ~(vec args) ~callback ~opts))
