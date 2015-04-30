(ns echo.response-test
  (:require [clojure.test :refer :all]
            [echo.response :refer :all]
            [echo.schemas :as schemas]))

(use-fixtures :once schema.test/validate-schemas)

(deftest cards
  (testing "with all parameters"
    (let [card-type "Simple"
          title "test-title"
          subtitle "test-subtitle"
          content "test-content"
          built-card (make-card card-type title subtitle content)
          expected-card {"type" card-type
                         "title" title
                         "subtitle" subtitle
                         "content" content}]
      (is (= expected-card built-card))))
  (testing "simple helper"
    (let [title "test-title"
          subtitle "test-subtitle"
          content "test-content"
          built-card (simple-card title subtitle content)
          expected-card {"type" "Simple"
                         "title" title
                         "subtitle" subtitle
                         "content" content}]
      (is (= expected-card built-card)))))

(deftest speeches
  (testing "with all parameters"
    (let [speech-type "PlainText"
          words "test words"
          built-speech (make-speech speech-type words)
          expected-speech {"type" speech-type
                           "text" words}]
      (is (= expected-speech built-speech))))
  (testing "simple helper"
    (let [words "test words"
          built-speech (simple-speech words)
          expected-speech {"type" "PlainText"
                           "text" words}]
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
  (testing "with parameters"
    (let [card (simple-card "test-title" "test-subtitle" "test-content")
          speech (simple-speech "test words")
          attributes {"new-attr" "new-val"}
          response (respond mock-session {:card card :speech speech :should-end? false
                                          :attributes attributes})
          expected-response {"version" "1.0"
                             "sessionAttributes" attributes
                             "response" {"shouldEndSession" false
                                         "card" card
                                         "outputSpeech" speech}}]
      (is (= expected-response response)))))
