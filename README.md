## lxscala-2017 ##

Supporting code for LXScala presentation from e.Near. 


This project contains (or will contain) various components to showcase the usage of Apache Kafka and both Akka-streams and FS" streaming libraries.


## Twitter Producer ##

Produces a stream of tweets (using Twitter streaming API) to a Kafka topic.

To run this component you need to configure a twitter consumer key and access token. You can get credentials from http://dev.twitter.com. You need to define for environment variables:

* `TWITTER_CONSUMER_KEY`
* `TWITTER_CONSUMER_SECRET`
* `TWITTER_ACCESS_TOKEN`
* `TWITTER_ACCESS_SECRET`

## Slides ##

This contains the project slides. Html ([reveal.js][revealj]) is supported, for now.

* Slides are written using Markdown
* Tut is used to pre-process the slides, compiling and running scala code
* [Pandoc][pandoc] converts markdown to html

To produce the slides:
* in the SBT shell change to the slides project: `sbt slides`.
* Run `slideshtml` task.


To produce the slides

## Kafka-docker ##

[here][https://github.com/wurstmeister/kafka-docker] you can find a docker-compose file to run Kafka and Zookeeper, mostly for testing. This is also added as a git submodule for convenience.



[revealjs]: http://lab.hakim.se/reveal-js/#/
[pandoc]: http://pandoc.org