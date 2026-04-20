package fp.smartWellness.util

import scalikejdbc.*
import java.time.LocalDateTime

object SessionManager extends Database:

  def initializeTable(): Unit =
    DB autoCommit { implicit s =>
      sql"""
        CREATE TABLE LOGIN_SESSION (
          session_id INT NOT NULL GENERATED ALWAYS AS IDENTITY
            (START WITH 1, INCREMENT BY 1),
          user_id INT NOT NULL,
          login_time TIMESTAMP,
          active BOOLEAN,
          PRIMARY KEY (session_id)
        )
      """.execute.apply()
    }

  def login(userId: Int): Unit =
    DB autoCommit { implicit s =>
      sql"UPDATE LOGIN_SESSION SET active = false".update.apply()
      sql"""
        INSERT INTO LOGIN_SESSION (user_id, login_time, active)
        VALUES ($userId, ${LocalDateTime.now}, true)
      """.update.apply()
    }

  def logout(): Unit =
    DB autoCommit { implicit s =>
      sql"UPDATE LOGIN_SESSION SET active = false".update.apply()
    }

  def currentUserId: Option[Int] =
    try
      DB readOnly { implicit s =>
        sql"""
        SELECT user_id FROM LOGIN_SESSION
        WHERE active = true
        ORDER BY login_time DESC
      """.map(_.int("user_id")).single.apply()
      }
    catch
      case _: Throwable => None

  def isLoggedIn: Boolean =
    currentUserId.isDefined
