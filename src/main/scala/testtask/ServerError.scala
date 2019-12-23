package testtask

trait ServerError

object ServerError {
  private case class Err(s: String) extends ServerError
  def apply(s: String): ServerError = Err(s)
  def unapply(arg: ServerError): Option[String] = arg match {
    case Err(s) => Some(s)
    case _ => None
  }
}