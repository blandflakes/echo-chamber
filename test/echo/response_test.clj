(ns echo.response-test
  (:require [clojure.test :refer :all]
            [echo.response :refer :all]
            [echo.schemas :as schemas]))

(use-fixtures :once schema.test/validate-schemas)

(deftest cards
  (testing "simple"
    (let [title "test-title"
          subtitle "test-subtitle"
          content "test-content"
          built-card (simple-card title subtitle content)
          expected-card {"type" "Simple"
                         "title" title
                         "subtitle" subtitle
                         "content" content}]
      (is (= expected-card built-card))))
  (testing "link-account"
    (let [built-card (link-account-card)
          expected-card {"type" "LinkAccount"}]
      (is (= expected-card built-card)))))

(deftest speeches
  (testing "plain-text"
    (let [words "test words"
          built-speech (plain-text-speech words)
          expected-speech {"type" "PlainText"
                           "text" words}]
      (is (= expected-speech built-speech))))
  (testing "ssml"
    (let [ssml "this should be markup"
          built-speech (ssml-speech ssml)
          expected-speech {"type" "SSML"
                           "ssml" ssml}]
      (is (= expected-speech built-speech)))))

(def mock-session
  {"new" true
   "sessionId" "sessionId"
   "attributes" {"attr" "val"}
   "user" {"userId" "test-user"}})

(deftest responses
  (testing "with no parameters"
    (let [response (respond mock-session)
          expected-response {"version" "1.0"
                             "sessionAttributes" {"attr" "val"}
                             "response" {"shouldEndSession" true}}]
      (is (= expected-response response))))
  (testing "with original parameters (plaintext speech, simple card)"
    (let [card (simple-card "test-title" "test-subtitle" "test-content")
          speech (plain-text-speech "test words")
          attributes {"new-attr" "new-val"}
          response (respond mock-session {:card card :speech speech :should-end? false
                                          :attributes attributes})
          expected-response {"version" "1.0"
                             "sessionAttributes" attributes
                             "response" {"shouldEndSession" false
                                         "card" card
                                         "outputSpeech" speech}}]
      (is (= expected-response response))))
  (testing "with alternate types (ssml speech, link-account card)"
    (let [card (link-account-card)
          speech (ssml-speech "test words")
          attributes {"new-attr" "new-val"}
          response (respond mock-session {:card card :speech speech :should-end? false
                                          :attributes attributes})
          expected-response {"version" "1.0"
                             "sessionAttributes" attributes
                             "response" {"shouldEndSession" false
                                         "card" card
                                         "outputSpeech" speech}}])))
