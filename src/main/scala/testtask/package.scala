import cats.data.EitherT
import cats.effect.IO

package object testtask {
  type Result[A] = EitherT[IO, ServerError, A]
}
