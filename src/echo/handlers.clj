(ns echo.handlers
  (:require [echo.response :as response]))


(defn unsupported-action
  "A default handler which builds a response in the case that a request or intent type couldn't be found.
  It is built-in to prevent erroneous interactions with the user, and will log the action."
  [_request]
  (response/respond
    (response/say "I'm not able to understand your request.")
    (response/card "Unable to understand request" "I'm not able to understand your request.")))