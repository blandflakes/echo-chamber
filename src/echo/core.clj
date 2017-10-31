(ns echo.core
  "Provides core functionality for building an echo app. To build an app, create a type
   that extends IEchoApp. Then call request-dispatcher to get an app that will dispatch
   based on the passed-in request type."
  (:require [echo.response :as response]
            [taoensso.timbre :as timbre :refer [error]]))

(def ^:private intent-request "IntentRequest")
(def launch "LaunchRequest")
(def end-session "SessionEndedRequest")

(defn- unsupported-action
  "A default handler which builds a response in the case that a request or intent type couldn't be found.
  It is built-in to prevent erroneous interactions with the user, and will log the action. This can be overridden
  by the developer by specifying a function with the key :unsupported-action."
  [request]
  (error "Unable to find a request or intent handler for request: " request)
  (let [speech (response/plaintext-speech "I'm not able to understand your request.")
        card (response/simple-card "Unable to understand request" "I'm not able to understand your request.")]
    (response/respond {:speech speech :card card :should-end? true})))

(defn dispatch
  "Given a skill-spec and a request, routes the request t to the appropriate handler. If the handler receives a request
  type or intent that the provided skill-spec does not support, it will provide an error message back to the user."
  [skill-spec echo-request]
  (let [request (get echo-request "request")
        request-type (get request "type")]
    (if (= request-type intent-request)
      (let [intent (get-in request ["intent" "name"])]
        (if-let [intent-handler (get-in skill-spec [:intents intent])]
          (intent-handler echo-request)
          ((or (:unsupported-action skill-spec) unsupported-action) request)))
      (if-let [request-handler (get-in skill-spec [:requests request-type])]
        (request-handler request)
        ((or (:unsupported-action skill-spec) unsupported-action) request)))))

(defn request-dispatcher
  "Given a skill-spec, returns a handler that accepts an Echo Request, routing it to the appropriate request
  or intent handler. If the handler receives a request type or intent that the provided skill-spec does not support,
  it will provide an error message back to the user."
  [skill-spec]
  (if (get-in skill-spec [:requests intent-request])
    (throw (IllegalArgumentException. "Do not dispatch on IntentRequest. Use :intents map instead."))
    (fn [echo-request] (dispatch skill-spec echo-request))))
