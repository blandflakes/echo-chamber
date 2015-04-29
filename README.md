# echo-chamber

A minimalist SDK for setting up a handler for implementing an Echo application server.

Follows the example of the Java SDK by requiring methods for handling each request type.

## Usage

This SDK is designed to be placed in between a web server (such as Ring or Compojure) and your application, providing
simple routing and schema validation, as well as response convenience methods.

To use the SDK, implement the IEchoApp protocol found in the echo/core namespace.
Then, get a dispatcher function by passing the app the `request-dispatcher` (also in echo/core).

This dispatcher expects to receive an EchoRequest structured map (NOT a json string). Your server or middleware
should handle deserializing that before you pass it to the dispatcher. See echo/schemas for more information on
the schemas to be provided.

Finally, your app should return an EchoResponse struct. The echo/response namespace has convenience functions
for creating this, the penultimate function being `respond`

To actually get this SDK, you'll have to build the jar and place it on the classpath, as it isn't published.
The easiest way is to use leiningen, like a sane person.

Check out the source, and then:

    lein jar && lein install

In your project, depend on echo-chamber:

    [echo-chamber "0.1.0-SNAPSHOT"]

And you're good to go.

## TODO
- Unit tests
- Sample usage (see Usage above)
- Publish so that lein can pick it up

## Future enhancements
- Potentially an intent dispatcher
- Breakout modes for common interactions - i.e. a confirm interaction could be modeled for me.
