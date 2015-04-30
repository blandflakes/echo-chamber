(ns echo.core-test
  (:require [clojure.test :refer :all]
            [echo.core :refer :all]
            [echo.response :as response]
            [schema.core :as s]))

; Schema validation is turned on for each request in this test so that we can test the missing request handler.
; If I enabled validation via fixtures, the validation would fail before the missing-handler could fire!

(deftype EchoingEchoApp []
  IEchoApp
  (on-launch [this request session] (response/respond session {:should-end? false :speech (response/simple-speech "launch")}))
  (on-intent [this request session] (response/respond session {:should-end? true :speech (response/simple-speech (get-in request ["intent" "name"]))}))
  (on-end [this request session] (response/respond session {:should-end? true})))

(def dispatcher (request-dispatcher (EchoingEchoApp.)))

(deftest launch-routing
  (testing "launch calls the on-launch function"
    (let [request {"version" "1.0"
                   "session" {"new" true
                              "sessionId" "sessionId"
                              "attributes" {}
                              "user" {"userId" "userId"}}
                   "request" {"type" "LaunchRequest"
                              "requestId" "requestId"}}
          response (s/with-fn-validation (dispatcher request))
          expected-response {"version" "1.0"
                             "sessionAttributes" {}
                             "response" {"shouldEndSession" false
                                         "outputSpeech" {"type" "PlainText"
                                                         "text" "launch"}}}]
      (is (= expected-response response)))))

(deftest intent-routing
  (testing "intent calls the on-intent function"
    (let [request {"version" "1.0"
                   "session" {"new" true
                              "sessionId" "sessionId"
                              "attributes" {}
                              "user" {"userId" "userId"}}
                   "request" {"type" "IntentRequest"
                              "requestId" "requestId"
                              "intent" {"name" "DoStuffIntent"
                                        "slots" {}}}}
          response (s/with-fn-validation (dispatcher request))
          expected-response {"version" "1.0"
                             "sessionAttributes" {}
                             "response" {"shouldEndSession" true 
                                         "outputSpeech" {"type" "PlainText"
                                                         "text" "DoStuffIntent"}}}]
      (is (= expected-response response)))))

(deftest end-routing
  (testing "end calls the on-end function"
    (let [request {"version" "1.0"
                   "session" {"new" false
                              "sessionId" "sessionId"
                              "attributes" {}
                              "user" {"userId" "userId"}}
                   "request" {"type" "SessionEndedRequest"
                              "requestId" "requestId"
                              "reason" "Requested"}}
          response (s/with-fn-validation (dispatcher request))
          expected-response {"version" "1.0"
                             "sessionAttributes" {}
                             "response" {"shouldEndSession" true}}]
      (is (= expected-response response)))))

(deftest missing-request-type-routing
  (testing "unexpected request types don't crash"
    (let [request {"version" "1.0"
                   "session" {"new" false
                              "sessionId" "sessionId"
                              "attributes" {}
                              "user" {"userId" "userId"}}
                   "request" {"type" "RandomRequest"
                              "requestId" "requestId"}}
          response (dispatcher request)
          expected-response {"version" "1.0"
                             "sessionAttributes" {}
                             "response" {"shouldEndSession" true
                                         "outputSpeech" {"type" "PlainText"
                                                         "text" "I'm not able to understand your request"}
                                         "card" {"type" "Simple"
                                                 "title" "Internal Error"
                                                 "subtitle" "Bad request type"
                                                 "content" "Unable to handle request type RandomRequest"}}}]
      (is (= expected-response response)))))
