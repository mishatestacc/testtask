package testtask

import cats.effect.IO
import io.circe._
import org.http4s._
import org.http4s.circe._

trait UserApi[CreateUser] {
  def create(cu: CreateUser): Result[User]

  def get(email: String): Result[User]

  def delete(email: String): Result[Unit]
}

case class User(id: Int, email: String, firstName: String, lastName: String)

object User {

  case class ReqResNotFound(id: Int) extends ServerError
  case class ReqResEmailNotMatch(id: Int, email: String) extends ServerError

  case class DbNotFound(email: String) extends ServerError

  implicit val userDecoder: Decoder[User] =
    Decoder.forProduct4("id", "email", "first_name", "last_name")(User.apply)

  implicit val userEncoder: Encoder[User] =
    Encoder.forProduct4("id", "email", "first_name", "last_name")(User.unapply(_).get)

  implicit def userEntityDecoder: EntityDecoder[IO, User] = jsonOf

  implicit def userEntityEncoder: EntityEncoder[IO, User] = jsonEncoderOf
}


case class CreateUser(id: Int, email: String)

object CreateUser {
  implicit val cuDecoder: Decoder[CreateUser] = Decoder
    .forProduct2("user_id", "email")(CreateUser.apply)

  implicit def cuEntityDecoder: EntityDecoder[IO, CreateUser] = jsonOf
}