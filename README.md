# echo-chamber

[![Clojars Project](http://clojars.org/echo-chamber/latest-version.svg)](http://clojars.org/echo-chamber)

A minimalist SDK for setting up a handler for implementing an Echo application server.

Follows the example of the Java SDK by requiring methods for handling each request type.

## Usage

This SDK is designed to be placed in between a web server (such as Ring) and your application, providing
simple intent routing (not to be confused with URL routing), as well as response convenience methods.

To use the SDK, create a map that specifies your handlers. There are currently three supported keys:

`requests`
The `requests` field specifies a map of string request-name to function. The function should take a single
echo request, and return an echo response.
[This](https://developer.amazon.com/docs/custom-skills/request-and-response-json-reference.html) is a good reference
for the return types.

`intents`
Identical to the `requests` map in function, except using intent-name for the key.

Then, get a dispatcher function by passing the spec to `request-dispatcher` (also in echo/core). The dispatcher
wraps your map with a handler that will route requests to your request handlers, using the request type as the key.
IntentRequest, however, will be routed to your intent handlers, using the name of the intent.

This dispatcher expects to receive an EchoRequest structured map (NOT a json string). Your server or middleware
should handle deserializing that before you pass it to the dispatcher.

The echo/response namespace has convenience functions for creating responses, the primary function being `respond`.

For help/an example, try using the [echo-chamber-template](http://github.com/blandflakes/echo-chamber-template)

## Future enhancements
- Lifecycle and data management. Right now, my sample app just manages its state within namespace. We'd like to at least be able to operate in a component-like manner.
- Spec integration. Since spec has no plans to support stringly-keyed maps, we'd likely need to implement a layer
between the handler and whatever server serves the app to convert map keys between symbols and keys.
- More response types. I've only added convenience methods as I need them, and honestly, there's probably a cleaner
way of specifying responses than I have. Open to feedback/experimentation here.
- A REPL-driven test workflow. Lots of online skill testers exist, but it would be even better if we could interact
with our app in the REPL, and at runtime.
