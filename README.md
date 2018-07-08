# echo-chamber

[![Clojars Project](http://clojars.org/echo-chamber/latest-version.svg)](http://clojars.org/echo-chamber)

A minimalist SDK for setting up a handler for implementing an Echo application server.

Patterned after the Alexa Skills Kit for Java.

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

For help/an example, try using the [echo-chamber-template](http://github.com/blandflakes/echo-chamber-template).
This template hosts a skill in a Servlet, with some required components for verification of requests. You can also
use this abstraction in AWS Lambda, though I haven't hosted any skills there yet and therefore have no sample code.

## Future enhancements
- Template directive response objects
- Dialog response objects
- Spec integration. Since spec has no plans to support stringly-keyed maps, we'd likely need to implement a layer
between the handler and whatever server serves the app to convert map keys between symbols and keys.
- A REPL-driven test workflow. Lots of online skill testers exist, but it would be even better if we could interact
with our app in the REPL, and at runtime. Could also start with some test utils.
- Potential move to [Clojure `deps` and `cli`](https://clojure.org/guides/deps_and_cli)

## Status

I very lazily develop this "SDK", as I don't find it extremely interesting. As I encounter use cases in my own projects,
I may add things. Please feel free to contribute, as there's no guarantee I'm going to build any of the things in this
document.