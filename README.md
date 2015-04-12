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

## Future enhancements
- Potentially an intent dispatcher
- Breakout modes for common interactions - i.e. a confirm interaction could be modeled for me.
- Oneshot apps?

## General Alexa app-building feedback:
* Some apps don't really want intents or slots, but could do quite a bit with the recognition-only. See professor-elemental for the hack I did to recreate the recognition (order slots alphabetically, brute force supply data, reorder slots when they come in).
* It seems impossible to say something on exit, as Alexa doesn't do anything with a response to SessionEndedRequest
* Is it possible to put multiple words to a slot? Documentation on that would be useful (apologies if it's in the docs, I read most of them but it's totally possible I missed something).
* It seems like the accuracy is greatly improved if my samples include more real-world examples. However, it's impossible to exhaust some of them (i.e. capturing arbitrary sentences). Is there a better way to do this?
* Switching on intent seems like a natural extension of an SDK. Most apps are going to want to do this.
* Other common features that apps may want to leverage would be things like:
  * mode support (i.e. confirm something without having to build an entire response object)

