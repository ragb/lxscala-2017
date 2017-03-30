## lxscala-2017 ##

Supporting code for LXScala presentation from e.Near. 


This project contains (or will contain) various components to showcase the usage of Apache Kafka and both Akka-streams and FS" streaming libraries.


## Twitter Producer 

Produces a stream of tweets (using Twitter streaming API) to a Kafka topic.

To run this component you need to configure a twitter consumer key and access token. You can get credentials from http://dev.twitter.com. You need to define for environment variables:

* `TWITTER_CONSUMER_KEY`
* `TWITTER_CONSUMER_SECRET`
* `TWITTER_ACCESS_TOKEN`
* `TWITTER_ACCESS_SECRET`

