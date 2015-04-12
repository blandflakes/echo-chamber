(ns echo.core
  "Provides core functionality for building an echo app. To build an app, create a type
   that extends IEchoApp. Then call request-dispatcher to get an app that will dispatch
   based on the passed-in request type."
  (:require [echo.response :as response]
            [echo.schemas :as schemas]
            [schema.core :as s]))

(defn- missing-request-type
  "Builds a response in the case that a request type couldn't be found.
   This is less necessary if we can return error codes, but for now it looks like
   we need to handle our own error reporting. Might be unnecessary or less useful
   if I apply schema validation to the input to request-dispatcher."
  [request-type session]
  (let [speech (response/simple-speech "I'm not able to understand your request")
        card (response/simple-card "Internal Error"
                                   "Bad request type"
                                   (str "Unable to handle request type " request-type))]
    (response/respond session {:speech speech :card card :should-end? true})))

(defprotocol IEchoApp
  "An application that responds to voice requests."
  (on-launch [this request session] "Called when the user launches the application.")
  (on-intent [this request session] "Called when the user specifies an intent.")
  (on-end [this request session] "Called when the sessions ends."))

(defn request-dispatcher
  "Wraps the app in a dispatcher that calls the appropriate method
   based on the provided request type. Takes as input an EchoRequest (see echo.schema namespace)
   and returns an EchoResponse."
  ([app]
   (s/with-fn-validation
     (s/fn :- schemas/EchoResponse
       [echo-request :- schemas/EchoRequest]
       (let [request (get echo-request "request")
             session (get echo-request "session")
             request-type (get request "type")]
         (cond
          (= request-type "LaunchRequest") (on-launch app request session)
          (= request-type "IntentRequest") (on-intent app request session)
          (= request-type "SessionEndedRequest") (on-end app request session)
          :else (missing-request-type request-type session)))))))
