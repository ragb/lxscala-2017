package co.enear.lxscala.twitter

import co.enear.lxscala.twitter.config._

import io.circe._
import fs2._
import org.http4s.client.blaze._
import org.http4s.client._
import org.http4s._
import org.http4s.dsl._
import jawnfs2._
import cats.data.Reader

sealed trait Query

object Query {
  final case class PhraseQuery(phrase: String) extends Query
}

trait TwitterStreamingClient {
  def jsonStream(query: Query): Stream[Task, Json]
}

object TwitterStreamingClient {
  implicit def reader[A](implicit twitterConfigReader: Reader[A, TwitterConfig]): Reader[A, TwitterStreamingClient] = DefaultTwitterStreamingClient.reader[A]
}

final case class DefaultTwitterStreamingClient(twitterConfig: TwitterConfig) extends TwitterStreamingClient {
  implicit val facade = jawn.CirceSupportParser.facade

  override def jsonStream(query: Query): Stream[Task, Json] = Stream.eval(createClient)
    .flatMap { httpClient =>
      requestStream(httpClient, query)
        .onFinalize(httpClient.shutdown)
    }

  private def requestStream(client: Client, query: Query): Stream[Task, Json] =
    Stream.eval(twitterRequest(query))
      .flatMap { request =>
        client.streaming[Json](request) { response =>
          if (response.status.isSuccess) {
            response.body.chunks
              .parseJsonStream
          } else {
            Stream.fail(new Exception(s"HTTP error ${response.status}"))
          }
        }
      }

  private def createClient = Task.delay { SimpleHttp1Client() }

  private val statusFilterUri = Uri.uri("https://stream.twitter.com/1.1/statuses/filter.json")
  private val oauthConsumer = oauth1.Consumer(twitterConfig.auth.consumerKey, twitterConfig.auth.consumerSecret)
  private val oauthToken = oauth1.Token(twitterConfig.auth.accessToken, twitterConfig.auth.accessSecret)

  private def twitterRequest(query: Query): Task[Request] = query match {
    case Query.PhraseQuery(words) => for {
      request <- POST(statusFilterUri, UrlForm("track" -> words))
      signedRequest <- oauth1.signRequest(request, oauthConsumer, None, None, Some(oauthToken))
    } yield signedRequest
  }

}
object DefaultTwitterStreamingClient {
  implicit def reader[A](implicit twiterConfigReader: Reader[A, TwitterConfig]): Reader[A, TwitterStreamingClient] = twiterConfigReader.map(DefaultTwitterStreamingClient.apply)
}
