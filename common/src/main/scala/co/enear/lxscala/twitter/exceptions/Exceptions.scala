package co.enear.lxscala.twitter.exceptions

object Exceptions {

  case class ParsingException(message: String) extends Exception(message)

  case class TweetWithoutUserException(message: String) extends Exception(message)

}