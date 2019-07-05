# echo-chamber

[![Clojars Project](http://clojars.org/echo-chamber/latest-version.svg)](http://clojars.org/echo-chamber)

A minimalist, zero-dependency Clojure SDK for implementing an Echo skill.

Patterned after the Alexa Skills Kit for Java, this SDK enables dispatching of requests via various
matchers and provides helpers for various response shapes. A `skill` function manages dispatch of requests
based on the provided `(matcher handler)` tuples.

## Sample Code

This library aims to be really succinct, but also really straightforward. It defers to basic abstractions rather
than macros and "magic". The primitives are matchers, which are predicates, handlers, which are functions which act
on a provided request envelope (a map), and return values which are also maps.

    (ns hello-world
      (:require [echo.core :refer [skill]]
                [echo.match :refer [intent]]
                [echo.response :refer [respond say card]]))
    (def my-app (skill
                  (intent "HelloWorldIntent") (respond
                                               (say "Hello, world!")
                                               (card "Introduction" "Hello, world!"))
                  (else) (respond
                          (say "Unsupported action!")))


## Usage

This SDK is designed to be placed in between a web server (such as Ring or AWS Lambda) and your skill logic, providing
simple request/intent routing (not to be confused with URL routing), as well as response convenience methods.

Skills are modeled as functions which take a deserialized request envelope and return a response object.

[This](https://developer.amazon.com/docs/custom-skills/request-and-response-json-reference.html) is a good reference
for the request and response shapes.

### `core`

You can write a "master function" by hand, or you can use the `skill` function in the `core` namespace. This function
takes a seq of predicate/handler functions and returns a unified skill function. This function will attempt to match the
input against each predicate in turn. When it finds a match, it will call the associated handler.


### `match`

This namespace contains some predefined predicates for matching on intent or request type. You can combine
matchers using Clojure's built-in `and` and `or` options - as long as every other form is a predicate, this library
doesn't care.

### `response`

While you could easily create a map manually which contains the response object, this namespace contains some useful
conveniences. It has several methods for constructing some common components of the return object. It also contains
several middleware-style methods which modify the response object, and allow a semi-declarative DSL for constructing
responses.

## Deploying

This library is a small core designed to be run "inside" of some hosting solution. Typically, this would be
an HTTP server or inside something like AWS Lambda.

I've developed a minimal web server that can host multiple skills called [echo-chamber-server](https://github.com/blandflakes/echo-chamber-server). [echo-chamber-template](http://github.com/blandflakes/echo-chamber-template) can be used to generate new projects using this server.

For a fully-integrated solution using both `echo-chamber` and `echo-chamber-server`, see [echo-stopwatch](https://github.com/blandflakes/echo-stopwatch)

This library should also be usable within AWS Lambda or any arbitrary middleware that can deserialize JSON requests to Clojure(script) data structures. However, I haven't yet hosted any skills in those paradigms, so I don't have any sample code to share.

## Future enhancements
- A REPL-driven test workflow. Lots of online skill testers exist, but it would be even better if we could interact
with our app in the REPL, and at runtime. Could also start with some test utils.
- Potential move to [Clojure `deps` and `cli`](https://clojure.org/guides/deps_and_cli)

## Status

I very lazily develop this "SDK", as I don't find it extremely interesting. As I encounter use cases in my own projects, I may add things. Please feel free to contribute, fork, etc!

## License

MIT, I guess!
