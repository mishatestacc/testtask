package testtask

import cats.effect.IO
import io.circe.Json
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

object UserRoutes {

  val dsl = new Http4sDsl[IO] {}

  import dsl._

  implicit class RichApiCall[A](val call: Result[A]) extends AnyVal {
    private def msg(s: String) = Json.obj("message" -> Json.fromString(s))

    def toResponse(response: A => IO[Response[IO]]): IO[Response[IO]] = call.value.flatMap {
      case Right(a) => response(a)
      case Left(User.ReqResNotFound(id)) => NotFound(msg(s"user $id not found in reqres"))
      case Left(User.ReqResEmailNotMatch(id, email)) => BadRequest(msg(s"user $id in reqres has different to '$email' email"))
      case Left(User.DbNotFound(email)) => NotFound(msg(s"user $email not found in local database"))
      case Left(ServerError(s)) => InternalServerError(msg(s"server error: $s"))
    }
  }

  def routes(api: UserApi[CreateUser]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "users" =>
        req.decode[CreateUser] { cu =>
          api.create(cu).toResponse(_ => Ok())
        }

      case GET -> Root / "users" / email =>
        api.get(email).toResponse(Ok(_))

      case DELETE -> Root / "users" / email =>
        api.delete(email).toResponse(_ => Ok())
    }
  }
}
