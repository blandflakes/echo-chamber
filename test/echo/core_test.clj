(ns echo.core-test
  (:require [clojure.test :refer :all]
            [echo.core :refer [skill]]
            [echo.match :refer [intent request-type else]]
            [echo.response :refer [respond end-session say]]))

(def app (skill
           (request-type "LaunchRequest") (fn [_request] (respond
                                                           (say "You've launched the app!")))
           (request-type "SessionEndedRequest") (fn [_request] (respond
                                                                 (end-session true)))
           (intent "GreetIntent") (fn [request]
                                    (let [name (get-in request ["request" "intent" "slots" "name"])]
                                      (respond
                                        (say (str "Hello, " name)))))))

(deftest launch-routing
  (testing "launch calls the on-launch function"
    (let [request {"version" "1.0"
                   "session" {"new"         true
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "LaunchRequest"
                              "requestId" "requestId"
                              "timestamp" "2015-05-13T12:34:56Z"}}
          response (app request)
          expected-response {"version"  "1.0"
                             "response" {"outputSpeech" {"type" "PlainText"
                                                         "text" "You've launched the app!"}}}]
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
                              "intent"    {"name"  "GreetIntent"
                                           "slots" {"name" "Francis"}}}}
          response (app request)
          expected-response {"version"  "1.0"
                             "response" {"outputSpeech" {"type" "PlainText"
                                                         "text" "Hello, Francis"}}}]
      (is (= expected-response response)))))

(deftest end-routing
  (testing "end calls the on-end function"
    (let [request {"version" "1.0"
                   "session" {"new"         false
                              "sessionId"   "sessionId"
                              "application" {"applicationId" "applicationId"}
                              "attributes"  {}
                              "user"        {"userId" "userId"}}
                   "request" {"type"      "SessionEndedRequest"
                              "requestId" "requestId"
                              "timestamp" "2015-05-13T12:34:56Z"
                              "reason"    "Requested"}}
          response (app request)
          ; even though ASK ends sessions by default, we'll set it explicitly.
          expected-response {"version"  "1.0"
                             "response" {"shouldEndSession" true}}]
      (is (= expected-response response)))))

(deftest error-handling
  (testing "no matching handler throws an exception"
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
                              "slots" {}}}]
      (is (thrown? Exception (app request))))))

(deftest skill-test
  (testing "requires an even number of tuples"
    (is (thrown? AssertionError (skill (else))))))