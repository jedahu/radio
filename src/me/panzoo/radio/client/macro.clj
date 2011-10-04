(ns me.panzoo.radio.client.macro)

(defmacro call
  "Call the remote function denoted by `name` with `args`, and on completion
  call `callback`.

  `callback` must be a function of two arguments. The first is `nil` on success
  and otherwise one of `:unauthorized`, `:unresolved`, `:server-error`, or
  `:error`, which correspond to the response codes 401, 501, 500, and any other
  error code. The second is a clojure data structure read in from the text of
  the HTTP response.

  `opts` is a map which may contain a `:priority` key with an integer value.
  This value is passed on as the priority argument to
  goog.net.XhrIoPool#getObject()."
  [[name & args] & [callback & [opts]]]
  `(me.panzoo.radio.client/rpc-call '~name ~(vec args) ~callback ~opts))
