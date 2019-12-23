package testtask

import cats.data.EitherT
import cats.effect.IO
import org.http4s.Response
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class UserRoutesSpec extends AnyFreeSpec with Matchers with MockFactory {

  import UserRoutes._
  import UserRoutes.dsl._

  implicit class RichResp(resp: Response[IO]) {
    def text: String = resp
      .bodyAsText
      .compile
      .string
      .unsafeRunSync()
  }

  def fromError(e: ServerError): Response[IO] =
    EitherT(IO.pure(Left(e).withRight[String]))
      .toResponse(Ok(_))
      .unsafeRunSync()

  def error(s: String): String = s"""{"message":"$s"}"""

  "RichApiCall" - {
    "toResponse" - {
      "Right" in {
        val resp = EitherT(IO.pure(Right("a").withLeft[ServerError]))
          .toResponse(Ok(_))
          .unsafeRunSync()
        resp.status shouldBe Ok
        resp.text shouldBe "a"
      }
      "ReqResNotFound" in {
        val resp = fromError(User.ReqResNotFound(1))
        resp.status shouldBe NotFound
        resp.text shouldBe error("user 1 not found in reqres")
      }
      "ReqResEmailNotMatch" in {
        val resp = fromError(User.ReqResEmailNotMatch(1, "a"))
        resp.status shouldBe BadRequest
        resp.text shouldBe error("user 1 in reqres has different to 'a' email")
      }
      "DbNotFound" in {
        val resp = fromError(User.DbNotFound("a"))
        resp.status shouldBe NotFound
        resp.text shouldBe error("user a not found in local database")
      }
      "ServerError" in {
        val resp = fromError(ServerError("a"))
        resp.status shouldBe InternalServerError
        resp.text shouldBe error("server error: a")
      }
    }
  }
}
