package fp.smartWellness.util

import scalikejdbc.*

trait Database:

  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:/Users/weimin/derby/myDB;create=true"

  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "user", "database")

  given AutoSession: DBSession = AutoSession

object Database extends Database:
  def setupDB(): Unit =
    val tables = Seq("USERS", "MOOD", "SLEEP", "ACHIEVEMENT", "LOGIN_SESSION")
    tables.foreach { table =>
      if !(DB getTable table).isDefined then
        table match
          case "USERS" => fp.smartWellness.model.UserModel.initializeTable()
          case "MOOD" => fp.smartWellness.model.MoodModel.initializeTable()
          case "SLEEP" => fp.smartWellness.model.SleepModel.initializeTable()
          case "ACHIEVEMENT" => fp.smartWellness.model.AchievementModel.initializeTable()
          case "LOGIN_SESSION" => fp.smartWellness.util.SessionManager.initializeTable()
    }

