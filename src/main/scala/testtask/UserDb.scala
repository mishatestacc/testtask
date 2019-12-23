package testtask

import java.sql.SQLException

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.{idiom => _, _}

class UserDb(xa: Transactor.Aux[IO, Unit]) extends UserApi[User] {
  val dc = new DoobieContext.Postgres(SnakeCase)

  import dc._

  type R[A] = testtask.Result[A] // to differ Result from dc._

  private val users = quote(querySchema[User]("users"))
  private def byEmail(email: String) = quote {
    users.filter(u => u.email == lift(email))
  }

  private def process[A](conn: ConnectionIO[A]): R[A] = {
    val result = conn
      .transact(xa)
      .map(a => Right(a).withLeft[ServerError])
      .recover {
        case e: SQLException => Left(ServerError(e.getMessage))
      }
    EitherT(result)
  }

  def create(user: User): R[User] = {
    val conn = run(users.insert(lift(user)))
    process(conn).map(_ => user)
  }

  def get(email: String): R[User] = {
    val conn = run(byEmail(email))
    process(conn).subflatMap(_.headOption.toRight(User.DbNotFound(email)))
  }

  def delete(email: String): R[Unit] = {
    val conn = run(byEmail(email).delete)
    process(conn).subflatMap(d => if (d > 0) Right(()) else Left(User.DbNotFound(email)))
  }
}


