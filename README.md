# echo-chamber

[![Clojars Project](http://clojars.org/echo-chamber/latest-version.svg)](http://clojars.org/echo-chamber)

A minimalist SDK for setting up a handler for implementing an Echo application server.

Follows the example of the Java SDK by requiring methods for handling each request type.

## Usage

This SDK is designed to be placed in between a web server (such as Ring) and your application, providing
simple intent routing (not to be confused with URL routing) and schema validation, as well as response convenience methods.

To use the SDK, implement the IEchoApp protocol found in the `echo/core` namespace.
Then, get a dispatcher function by passing the app the `request-dispatcher` (also in echo/core).

This dispatcher expects to receive an EchoRequest structured map (NOT a json string). Your server or middleware
should handle deserializing that before you pass it to the dispatcher.

Finally, your app should return an EchoResponse struct. The echo/response namespace has convenience functions for
creating this, the primary function being `respond`.

For help/an example, try using the [echo-chamber-template](http://github.com/blandflakes/echo-chamber-template)

## Future enhancements
- Replace schema with spec.
- Potentially an intent dispatcher instead of the user having to multimethod themselves
- It's possible macros or a DSL could make this more pleasant to work in, but I haven't conceived of a design for that
at this point. One opportunity may be intent dispatch - everybody writes the same multimethod definition. A macro could
assist here, similar to Compojure's routes.
- Consider dynamically binding session so it's not part of the signature. The session is just part of the request,
which we already send. Another macro/DSL opportunity?
- A REPL-driven test workflow. Lots of online skill testers exist, but it would be even better if we could interact
with our app in the REPL.
- Breakout modes for common interactions - i.e. a confirm interaction could be modeled for me.

