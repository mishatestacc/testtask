package testtask


class UserService(reqRes: ReqRes, db: UserApi[User]) extends UserApi[CreateUser] {
  def create(cu: CreateUser): Result[User] =
    reqRes
      .findUser(cu.id)
      .subflatMap(u => if (u.email == cu.email) Right(u) else Left(User.ReqResEmailNotMatch(cu.id, cu.email)))
      .flatMap(db.create)

  def get(email: String): Result[User] = db.get(email)

  def delete(email: String): Result[Unit] = db.delete(email)
}
