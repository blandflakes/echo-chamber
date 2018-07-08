(ns echo.core
  "Provides core functionality for building an echo app.")

(defn skill
  "Given a list of matcher/handler pairs, return a function which routes Echo request envelopes to the appropriate
  handler. Handlers are evaluated in the order provided."
  [& pairs]
  (assert (even? (count pairs)) "Must have a seq of pairs (i.e. an even number of filter/handler pairs)")
  (let [tuples (partition 2 pairs)]
    (fn [request-envelope]
      ; This is kind of gross... Might be worth creating a named record or something
      (if-let [handler
               (->>  tuples
                     ; lazy-seq of tuples whose matcher accepts this request
                     (filter (fn [[matcher _handler]] (matcher request-envelope)))
                     ; first matching tuple
                     first
                     ; unpack handler
                     second)]
        (handler request-envelope)
        (throw (ex-info "No suitable handler for request" request-envelope))))))