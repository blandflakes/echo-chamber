(ns echo.schemas
  (:require [schema.core :as s]))

(def AttributeMap {s/Str s/Any})

(def Session
  "Contains information about a user's session in a session app"
  {(s/required-key "new")         s/Bool
   (s/required-key "application") {(s/required-key "applicationId") s/Str}
   (s/required-key "sessionId")   s/Str
   (s/optional-key "attributes")  (s/maybe AttributeMap)
   (s/required-key "user")        {(s/required-key "userId")      s/Str
                                   (s/optional-key "accessToken") s/Str}})

(def BaseRequestParams
  "Represents the minimum required parameters in a request object."
  {(s/required-key "type")      s/Str
   (s/required-key "requestId") s/Str
   (s/required-key "timestamp") s/Str})


(def SlotMapping
  "Represents a mapping of slot names to slot objects."
  {s/Str {(s/required-key "name") s/Str (s/optional-key "value") s/Str}})

(def Intent
  "Represents an intent as passed in to an echo application."
  {(s/required-key "name")  s/Str
   (s/optional-key "slots") SlotMapping})

(def LaunchRequest (assoc BaseRequestParams (s/required-key "type") (s/eq "LaunchRequest")))
(def SessionEndedRequest (assoc BaseRequestParams
                           (s/required-key "type") (s/eq "SessionEndedRequest")
                           (s/required-key "reason") s/Str))
(def IntentRequest (assoc BaseRequestParams
                     (s/required-key "type") (s/eq "IntentRequest")
                     (s/required-key "intent") Intent))

(defn- request-is? [request possible-type]
  (= (get request "type") possible-type))

(def EchoRequest
  "Represents a request to an Echo application."
  {(s/required-key "version") s/Str
   (s/required-key "session") Session
   (s/required-key "request") (s/conditional #(request-is? % "LaunchRequest") LaunchRequest
                                             #(request-is? % "SessionEndedRequest") SessionEndedRequest
                                             #(request-is? % "IntentRequest") IntentRequest)})

(def PlainTextSpeech
  "Represents plain speech to be spoken by Alexa."
  {(s/required-key "type") (s/eq "PlainText")
   (s/required-key "text") s/Str})

(def SSMLSpeech
  "Represents speech specified in SSML."
  {(s/required-key "type") (s/eq "SSML")
   (s/required-key "ssml") s/Str})

(def OutputSpeech
  "Represents the portion of the response that contains instructions
   about speech."
  (s/either SSMLSpeech PlainTextSpeech))

(def SimpleCard
  "Represents the fields present in a simple card."
  {(s/required-key "type")    (s/eq "Simple")
   (s/optional-key "title")   (s/maybe s/Str)
   (s/optional-key "content") (s/maybe s/Str)})

(def LinkAccountCard
  "Represents the fields present in a card of type LinkAccount"
  {(s/required-key "type") (s/eq "LinkAccount")})

(def Card
  "Represents the portion of the resposne that contains instructions
   about displaying a card to the user."
  (s/either LinkAccountCard SimpleCard))

(def EchoResponse
  "Represents a response from an Echo application."
  {(s/required-key "version")           s/Str
   (s/optional-key "sessionAttributes") AttributeMap
   (s/required-key "response")          {(s/optional-key "outputSpeech")     OutputSpeech
                                         (s/optional-key "card")             Card
                                         (s/optional-key "reprompt")         OutputSpeech
                                         (s/required-key "shouldEndSession") s/Bool}})
