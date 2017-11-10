(ns echo.core-test
  (:require [clojure.test :refer :all]
            [echo.core :refer :all]
            [echo.response :as response]))

(def echo-spec {
                :requests {launch      (fn [request] (response/respond {:should-end? false
                                                                        :speech      (response/plaintext-speech (get-in request ["request" "type"]))}))
                           end-session (fn [_] (response/respond {:should-end? true}))}
                :intents  {"DoStuffIntent" (fn [request] (response/respond {:should-end? true
                                                                            :speech      (response/plaintext-speech
                                                                                           (get-in request ["request" "intent" "name"]))}))}})

(def dispatcher (request-dispatcher echo-spec))

(deftest launch-routing
  (testing "launch calls the on-launch function"
    (let [request {"version" "1.0"
                   "session" {"new"         true
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      launch
                              "requestId" "requestId"
                              "timestamp" "2015-05-13T12:34:56Z"}}
          response (dispatcher request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" false
                                         "outputSpeech"     {"type" "PlainText"
                                                             "text" "LaunchRequest"}}}]
      (is (= expected-response response)))))

(deftest intent-routing
  (testing "intent calls the on-intent function"
    (let [request {"version" "1.0"
                   "session" {"new"         true
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "IntentRequest"
                              "requestId" "requestId"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "intent"    {"name"  "DoStuffIntent"
                                           "slots" {}}}}
          response (dispatcher request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true
                                         "outputSpeech"     {"type" "PlainText"
                                                             "text" "DoStuffIntent"}}}]
      (is (= expected-response response)))))

(deftest end-routing
  (testing "end calls the on-end function"
    (let [request {"version" "1.0"
                   "session" {"new"         false
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      end-session
                              "requestId" "requestId"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "reason"    "Requested"}}
          response (dispatcher request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true}}]
      (is (= expected-response response)))))

(deftest missing-request-type-routing
  (testing "unexpected request types are handled gracefully"
    (let [request {"version" "1.0"
                   "session" {"new"         false
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "RandomRequest"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "requestId" "requestId"}}
          response (dispatcher request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true
                                         "outputSpeech"     {"type" "PlainText"
                                                             "text" "I'm not able to understand your request."}
                                         "card"             {"type"    "Simple"
                                                             "title"   "Unable to understand request"
                                                             "content" "I'm not able to understand your request."}}}]
      (is (= expected-response response)))))

(deftest missing-intent-routing
  (testing "unexpected intent types are handled gracefully"
    (let [request {"version" "1.0"
                   "session" {"new"         false
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "IntentRequest"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "requestId" "requestId"}
                   "intent"  {"name"  "RandomIntent"
                              "slots" {}}}
          response (dispatcher request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true
                                         "outputSpeech"     {"type" "PlainText"
                                                             "text" "I'm not able to understand your request."}
                                         "card"             {"type"    "Simple"
                                                             "title"   "Unable to understand request"
                                                             "content" "I'm not able to understand your request."}}}]
      (is (= expected-response response)))))

(def custom-missing-handler-spec (assoc echo-spec :unsupported-action
                                                  (fn [request]
                                                    (let [speech (response/plaintext-speech "Custom failure to handle request.")
                                                          card (response/simple-card "Custom unable to understand request"
                                                                                     "I'm not able to understand your request.")]
                                                      (response/respond {:speech speech :card card :should-end? true})))))

(deftest custom-missing-handler
  (testing "specifying a custom handler results in its being used"
    (let [request {"version" "1.0"
                   "session" {"new"         false
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "IntentRequest"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "requestId" "requestId"}
                   "intent"  {"name"  "RandomIntent"
                              "slots" {}}}
          response ((request-dispatcher custom-missing-handler-spec) request)
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true
                                         "outputSpeech"     {"type" "PlainText"
                                                             "text" "Custom failure to handle request."}
                                         "card"             {"type"    "Simple"
                                                             "title"   "Custom unable to understand request"
                                                             "content" "I'm not able to understand your request."}}}]
      (is (= expected-response response)))))

(def with-intent-dispatcher (assoc-in echo-spec [:requests "IntentRequest"] (fn [_] "Unused")))

(deftest illegal-intent-handler
  (testing "specifying a request handler for IntentRequest is disallowed")
  (is (thrown-with-msg? IllegalArgumentException #"Do not dispatch on IntentRequest. Use :intents map instead."
                        (request-dispatcher with-intent-dispatcher))))