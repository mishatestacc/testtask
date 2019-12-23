package testtask

import cats.effect._
import cats.implicits._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global


object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    stream.compile.drain.as(ExitCode.Success)
  }

  private def stream = {
    BlazeClientBuilder[IO](global).stream.flatMap { client =>
      val xa = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql:testdb",
        "postgres",
        "",
        Blocker.liftExecutionContext(ExecutionContexts.synchronous)
      )
      val userDb = new UserDb(xa)

      val reqRes = new ReqRes.Impl(client)
      val userApi = new UserService(reqRes, userDb)
      val httpApp = UserRoutes.routes(userApi).orNotFound
      val finalHttpApp = Logger.httpApp(true, true)(httpApp)
      BlazeServerBuilder[IO]
        .bindHttp(9000, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    }
  }
}
