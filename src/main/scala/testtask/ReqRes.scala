package testtask

import cats.data.EitherT
import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.Method._
import org.http4s.{Status, _}
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.client.dsl.io._

trait ReqRes {
  def findUser(id: Int): Result[User]
}

object ReqRes {

  case class Data(data: User)

  implicit val dataDecoder: Decoder[Data] = deriveDecoder[Data]

  implicit def dataEntityDecoder: EntityDecoder[IO, Data] = jsonOf

  class Impl(client: Client[IO]) extends ReqRes {

    override def findUser(id: Int): Result[User] = EitherT {
      val req = GET(Uri.unsafeFromString(s"https://reqres.in/api/users/$id"))
      client.fetch(req) {
        case Status.Successful(r) => r
          .attemptAs[Data]
          .map(_.data)
          .leftMap(e => ServerError(e.getMessage())).value
        case r =>
          if (r.status == Status.NotFound) IO.pure(Left(User.ReqResNotFound(id)))
          else r.as[String].map(r => Left(ServerError(s"Request $req failed with $r")))
      }
    }
  }

}