package in.oss.docker.manager.domain

object Syntax {

  extension (list: List[String]) def flat: String = list.mkString("'", ", ", "'")
}
