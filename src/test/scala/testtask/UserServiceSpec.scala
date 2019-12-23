package testtask

import cats.data.EitherT
import cats.effect.IO
import org.scalatest.freespec.AnyFreeSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers


class UserServiceSpec extends AnyFreeSpec with Matchers with MockFactory {

  val testUser = User(1, "a", "b", "c")

  implicit class wrapperA[A](a: A) {
    def lift = EitherT(IO.pure(Right(a).withLeft[ServerError]))
  }

  trait Base {
    val reqRes = stub[ReqRes]
    val db = stub[UserApi[User]]
    val service = new UserService(reqRes, db)
  }

  trait ReqResUser extends Base {
    (reqRes.findUser _) when(1) returns(testUser.lift)
  }
  "create" - {
    "ok" in new ReqResUser {
      (db.create _) when(*) onCall { u: User => u.lift }
      service.create(CreateUser(1, "a")).value.unsafeRunSync() shouldBe Right(testUser)
    }
    "not match" in new ReqResUser {
      service.create(CreateUser(1, "b")).value.unsafeRunSync() shouldBe Left(User.ReqResEmailNotMatch(1, "b"))
    }
  }
  "get" in new Base {
    (db.get _) when("a") returns(testUser.lift)
    service.get("a").value.unsafeRunSync() shouldBe Right(testUser)
  }
  "delete" in new Base {
    (db.delete _) when("a") returns(().lift)
    service.delete("a").value.unsafeRunSync() shouldBe Right(())
  }
}
