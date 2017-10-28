(ns echo.response
  (:require [echo.schemas :as schemas]
            [schema.core :as s]
            [schema.test]))

; Contains functions for generating a response
(s/defn simple-card :- schemas/SimpleCard
  "Builds a map representing a simple card from the provided entries."
  [title content]
  {"type"    "Simple"
   "title"   title
   "content" content})

(s/defn link-account-card :- schemas/LinkAccountCard
  "Builds a map representing a LinkAccountCard."
  []
  {"type" "LinkAccount"})

(s/defn with-card :- schemas/EchoResponse
  "Returns a version of the provided response with the card inserted
   into the response."
  [response :- schemas/EchoResponse
   card :- schemas/Card]
  (assoc-in response ["response" "card"] card))

(s/defn plaintext-speech :- schemas/PlainTextSpeech
  "Builds a map representing plaintext speech output with the provided type and text."
  [text]
  {"type" "PlainText"
   "text" text})

(s/defn ssml-speech :- schemas/SSMLSpeech
  "Builds a map representing speech specified by SSML."
  [ssml]
  {"type" "SSML"
   "ssml" ssml})

(s/defn with-speech :- schemas/EchoResponse
  "Returns a version of the provided response with the speech inserted
   into the response."
  [response :- schemas/EchoResponse
   speech :- schemas/OutputSpeech]
  (assoc-in response ["response" "outputSpeech"] speech))

(s/defn with-attributes :- schemas/EchoResponse
  "Returns a version of the provided response with its attributes
   replaced with the provided attributes."
  [response :- schemas/EchoResponse
   attributes :- schemas/AttributeMap]
  (assoc response "sessionAttributes" attributes))

(s/defn respond :- schemas/EchoResponse
  "Builds a complete response with the provided arguments.
   The argument is a hash of options. Supported values:
   :attributes should point to a map of {string object}, which will overwrite any existing session attribute map.
   :card should point to a valid card, or not be present if no card is sent
   :speech should point to valid speech, or not be present if no speech is sent
   :should-end? is whether the app session should close. Defaults to true.
   If the second map is not sent, a response with no card, no speech, and a should-end? value of true will be created."
  ([] (respond {}))
  ([{:keys [attributes card speech should-end?]
     :or   {should-end? true}}]
    (let [response {"version"  "1.0"
                    "response" {"shouldEndSession" should-end?}}
          response (if attributes
                     (with-attributes response attributes)
                     response)
          response (if speech
                     (with-speech response speech)
                     response)
          response (if card
                     (with-card response card)
                     response)]
      response)))
