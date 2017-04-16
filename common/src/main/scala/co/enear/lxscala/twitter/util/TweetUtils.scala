package co.enear.lxscala.twitter.util

import co.enear.lxscala.twitter.entities.Tweet
import co.enear.lxscala.twitter.exceptions.Exceptions.ParsingException
import io.circe.parser.parse
import cats.implicits._

object TweetUtils {
  final val noUserInTweetErrorMsg: (String) => String = { tweetId => s"Tweet with id $tweetId has no user and will be skipped" }

  def parseTweet[QueueMessage](message: QueueMessage, extractJS: QueueMessage => String): Tweet = {
    val tweetJS = parse(extractJS(message)).valueOr(parsingError => throw ParsingException(parsingError.message))
    tweetJS.as[Tweet].valueOr(parsingError => throw ParsingException(parsingError.message))
  }
}
