(ns echo.match)

(defn request-type
  "Predicate which matches if the request type matches the specified type"
  [target-type]
  (fn [request-envelope] (= target-type (get-in request-envelope ["request" "type"]))))

(defn intent
  "Predicate which matches if the requested intent matches the specified intent."
  [target-intent]
  ; since get-in is null-safe, we won't AND this with a (request-type "IntentRequest")
  (fn [request-envelope] (= target-intent (get-in request-envelope ["request" "intent" "name"]))))

(defn else
  "Convenience predicate which always returns true."
  []
  (fn [_request-envelope] true))