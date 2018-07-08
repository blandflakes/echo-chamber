(ns echo.response-test
  (:require [clojure.test :refer :all]
            [echo.response :refer :all]))

(deftest data-structures
  (testing "simple-card"
    (is (= {"type"    "Simple"
            "title"   "testTitle"
            "content" "testContent"}
           (simple-card "testTitle" "testContent"))))
  (testing "standard-card"
    (is (= {"type"  "Standard"
            "title" "testTitle"
            "text"  "testContent"
            "image" {"smallImageUrl" "http://small.jpg"
                     "largeImageUrl" "http://large.jpg"}}
           (standard-card "testTitle" "testContent" {:small "http://small.jpg" :large "http://large.jpg"}))))
  (testing "link-account-card"
    (is (= {"type" "LinkAccount"} (link-account-card))))

  (testing "plaintext-speech"
    (is (= {"type" "PlainText" "text" "hello"} (plaintext-speech "hello"))))
  (testing "ssml-speech"
    (is (= {"type" "SSML" "ssml" "<ssml>markup</ssml>"} (ssml-speech "<ssml>markup</ssml>"))))

  (testing "play-audio-directive"
    (is (= {"type" "AudioPlayer.Play" "playBehavior" "ENQUEUE" "audioItem" "http://audio.mp3"}
           (play-audio-directive "ENQUEUE" "http://audio.mp3"))))
  (testing "stop-audio-directive"
    (is (= {"type" "AudioPlayer.Stop"} (stop-audio-directive))))
  (testing "clear-audio-directive"
    (is (= {"type" "AudioPlayer.ClearQueue" "clearBehavior" "CLEAR_ALL"}
           (clear-audio-directive "CLEAR_ALL"))))

  (testing "launch-video-directive"
    (is (= {"type" "VideoApp.Launch" "videoItem" {"someKey" "someValue"}}
           (launch-video-directive {"someKey" "someValue"})))))

; Now that the builder helpers are verified, I can use them to make these tests less verbose
(defn ^:private check
  "Helper method which executes a middleware with a basic envelope and checks that the result is where it should be."
  ([middleware response-path value] (check middleware (envelope) response-path value))
  ([middleware starting-envelope response-path value]
   (let [returned (get-in (middleware starting-envelope) response-path)]
     (is (= value returned)))))

(deftest middleware
  (testing "card"
    (check (card "testTitle" "testText") ["response" "card"] (simple-card "testTitle" "testText"))
    (check (card "testTitle" "testContent" {:small "http://test.small" :large "http://test.large"})
           ["response" "card"]
           (standard-card "testTitle" "testContent" {:small "http://test.small" :large "http://test.large"})))
  (testing "link-account"
    (check (link-account) ["response" "card"] (link-account-card)))

  (testing "say"
    (check (say "hello") ["response" "outputSpeech"] (plaintext-speech "hello")))
  (testing "ssml"
    (check (ssml "<ssml>markup</ssml") ["response" "outputSpeech"] (ssml-speech "<ssml>markup</ssml")))

  (testing "reprompt"
    (check (reprompt "Come again?") ["response" "reprompt" "outputSpeech"] (plaintext-speech "Come again?")))
  (testing "reprompt-ssml"
    (check (reprompt-ssml "<ssml>Quoi?</ssml>") ["response" "reprompt" "outputSpeech"] (ssml-speech "<ssml>Quoi?</ssml>")))

  (testing "attributes"
    (check (attributes {"val" "key"}) ["sessionAttributes"] {"val" "key"}))

  (testing "end-session"
    (check (end-session true) ["response" "shouldEndSession"] true)
    (check (end-session false) ["response" "shouldEndSession"] false))

  (testing "directive"
    (check (directive {"key" "d1"}) ["response" "directives"] [{"key" "d1"}])
    (check (directive {"key" "d2"}) (-> (envelope) ((directive {"key" "d1"}))) ["response" "directives"]
           [{"key" "d1"} {"key" "d2"}]))

  (testing "play-audio"
    (check (play-audio "ENQUEUE" "http://test.mp3")
           ["response" "directives"]
           [(play-audio-directive "ENQUEUE" "http://test.mp3")]))
  (testing "stop-audio" (check (stop-audio) ["response" "directives"] [(stop-audio-directive)]))
  (testing "clear-audio" (check (clear-audio "CLEAR_ALL")
                                ["response" "directives"]
                                [(clear-audio-directive "CLEAR_ALL")]))

  (testing "launch-video"
    (check (launch-video {"link" "http://test.avi"})
           ["response" "directives"]
           [(launch-video-directive {"link" "http://test.avi"})])))