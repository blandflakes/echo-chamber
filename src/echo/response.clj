(ns echo.response
  "Contains functions for building responses.
  There are helper functions which create structs for various response types, as well as
  function which generate a middleware which accepts a response and modifies it with the requested
  response information. Developers have 3 options for creating responses:
  1. Create a response map (including each part) by hand
  2. Use the struct builders here, but still return a hand-associated map
  3. Use the middleware functions to create a chain of helpers. This option can result in more succinct code,
  at the cost of some clarity.

  There are not yet any helpers for the display/template directives - the 'directive' function can be used to
  inject them, but I elected to lazily write those at a time when I finally end up using one.
  ")

; Various response map creators
(defn envelope
  []
  "The basic container for a response prior to any customized response logic."
  {"version" "1.0"})

(defn simple-card
  [title content]
  {"type"    "Simple"
   "title"   title
   "content" content})

(defn standard-card
  "Creates a standard card. The image map should use keys :small and :large, which this function will
  convert to the appropriate response keys."
  [title text image]
  {"type"  "Standard"
   "title" title
   "text"  text
   "image" {"smallImageUrl" (:small image) "largeImageUrl" (:large image)}})

(defn link-account-card
  []
  {"type" "LinkAccount"})

(defn plaintext-speech
  [text]
  {"type" "PlainText"
   "text" text})

(defn ssml-speech
  [ssml]
  {"type" "SSML"
   "ssml" ssml})

(defn play-audio-directive
  "This is an opportunity for future work - the audioItem key of the Play directive is a fairly nested
  object. Aside from duplicating the structure in a function, I don't yet see a way to simplify it.
  For now, this requires the user to construct the audio object themselves."
  [behavior audio]
  {"type"         "AudioPlayer.Play"
   "playBehavior" behavior
   "audioItem"    audio})

(defn stop-audio-directive [] {"type" "AudioPlayer.Stop"})

(defn clear-audio-directive [behavior]
  {"type"          "AudioPlayer.ClearQueue"
   "clearBehavior" behavior})

(defn launch-video-directive
  "Like play-audio-directive, we require the user to construct their video, for now."
  [video]
  {"type"      "VideoApp.Launch"
   "videoItem" video})

; Middleware functions

(defn card
  "Returns a middleware which creates and inserts the requested card into a response map.
  If the arity with an image map is used, a Standard card will be returned, otherwise we'll use a simple card."
  ([title content] (fn [envelope] (assoc-in envelope ["response" "card"] (simple-card title content))))
  ([title text image] (fn [envelope] (assoc-in envelope ["response" "card"] (standard-card title text image)))))

(defn link-account
  "Returns a middleware which sets the response's card to a LinkAccountCard."
  []
  (fn [envelope] (assoc-in envelope ["response" "card"] (link-account-card))))

(defn say
  "Returns a middleware which inserts the provided speech into the response as Plaintext speech."
  [speech]
  (fn [envelope] (assoc-in envelope ["response" "outputSpeech"] (plaintext-speech speech))))

(defn ssml
  "Returns a which inserts the provided markup into the response as SSML speech."
  [markup]
  (fn [envelope] (assoc-in envelope ["response" "outputSpeech"] (ssml-speech markup))))

(defn reprompt
  "Returns a middleware which adds a Reprompt speech object. This object is used if the user does not
  response to a request by the device. Mirrors 'say' behavior for its inputs."
  [speech]
  (fn [envelope] (assoc-in envelope ["response" "reprompt" "outputSpeech"] (plaintext-speech speech))))

(defn reprompt-ssml
  "Returns a middleware which adds a Reprompt speech object. This object is used if the user does not
  response to a request by the device. Mirrors 'ssml' behavior for its inputs."
  [markup]
  (fn [envelope] (assoc-in envelope ["response" "reprompt" "outputSpeech"] (ssml-speech markup))))

(defn attributes
  "Returns a middleware which overwrites the attributes of this session with the new attributes."
  [new-attributes]
  (fn [envelope] (assoc envelope "sessionAttributes" new-attributes)))

(defn end-session
  "Returns a middleware which sets shouldEndSession to true or false. Alexa defaults to true for this."
  [end?]
  (fn [envelope] (assoc-in envelope ["response" "shouldEndSession"] end?)))

(defn directive
  "Returns a middleware which adds the given directive to the response."
  [directive]
  (fn [envelope]
    (let [current-directives (get-in envelope ["response" "directives"] [])
          updated-directives (conj current-directives directive)]
      (assoc-in envelope ["response" "directives"] updated-directives))))

(defn play-audio
  "Returns a middleware which generates and adds a Play directive to the response."
  [behavior audio]
  (directive (play-audio-directive behavior audio)))

(defn stop-audio
  "Returns a middleware which generates and adds a Stop directive to the response."
  []
  (directive (stop-audio-directive)))

(defn clear-audio
  "Returns a middleware which generates and adds a ClearQueue directive to the response."
  [behavior]
  (directive (clear-audio-directive behavior)))

(defn launch-video
  "Returns a middleware which generates and adds a LaunchVideo directive to the response."
  [video]
  (directive (launch-video-directive video)))

(defn respond
  "Helper for taking a chain of responses (responses are functions which operate on a map) and reducing them.
  The caller could essentially do the same with something like (-> (envelope) (say message)) and so on."
  [& responses]
  (reduce (fn [response-envelope response-middleware]
            (response-middleware response-envelope))
          (envelope) responses))