(ns echo.response)
; Contains functions for generating a response
(defn make-card
  "Builds a map representing a card from the provided entries."
  [card-type title subtitle content]
  {"type" card-type
   "title" title
   "subtitle" subtitle
   "content" content})

(defn with-card
  "Returns a version of the provided response with the card inserted
   into the response."
  [response card]
  (assoc-in response ["response" "card"] card))

(def simple-card (partial make-card "Simple"))

(defn make-speech
  "Builds a map representing speech output with the provided type and text."
  [speech-type text]
  {"type" speech-type
   "text" text})

(defn with-speech
  "Returns a version of the provided response with the speech inserted
   into the response."
  [response speech]
  (assoc-in response ["response" "outputSpeech"] speech))

(def simple-speech (partial make-speech "PlainText"))

(defn with-attributes
  "Returns a version of the provided response with its attributes
   replaced with the provided attributes."
  [response attributes]
  (assoc response "sessionAttributes" attributes))

(defn- bare-response
  "Builds a bare map representing a response to an Echo app interaction.
   This response has no card or speech.
   If should-end? isn't provided, it will default to false."
  ([session] (bare-response session false))
  ([session should-end?]
   {"version" "1.0"
    "sessionAttributes" (get session "attributes")
    "response" {"shouldEndSession" should-end?}})) 

(defn respond
  "Builds a complete response with the provided arguments.
   session is the session to respond to.
   The second argument is a hash of options. Supported values:
   :attributes should point to a map of {string object}, which will overwrite any existing session attribute map.
   :card should point to a valid card, or not be present if no card is sent
   :speech should point to valid speech, or not be present if no speech is sent
   :should-end? is whether the app session should close. Defaults to false.
   If the second map is not sent, a response with no card, no speech, and a should-end? value of false will be created.
   Uses the attributes supplied in the session if none are supplied."
  ([session] (respond session {}))
  ([session
   {:keys [attributes card speech should-end?]
    :or {should-end? false}}]
  (let [response (bare-response session should-end?)
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
