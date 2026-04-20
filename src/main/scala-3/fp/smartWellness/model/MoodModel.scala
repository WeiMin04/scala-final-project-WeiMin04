package fp.smartWellness.model

import scalafx.beans.property.*
import scalikejdbc.*
import fp.smartWellness.util.Database
import scala.util.Try

class MoodModel(
                 val userIdI: Int,
                 val moodTypeS: String,
                 val intensityI: Int,
                 val noteS: String,
                 val dateS: String
               ) extends Database {

  var userId    = IntegerProperty(userIdI)
  var moodType  = StringProperty(moodTypeS)
  var intensity = IntegerProperty(intensityI)
  var note      = StringProperty(noteS)
  var entryDate = StringProperty(dateS)

  /** Save mood record ONLY */
  def save(): Try[Int] =
    Try {
      DB autoCommit { implicit s =>
        sql"""
          INSERT INTO MOOD (
            user_id, mood_type, intensity, note, entry_date
          )
          VALUES (
            ${userId.value},
            ${moodType.value},
            ${intensity.value},
            ${note.value},
            ${entryDate.value}
          )
        """.update.apply()
      }
    }
}

object MoodModel extends Database {

  def initializeTable(): Unit =
    DB autoCommit { implicit s =>
      sql"""
        CREATE TABLE MOOD (
          mood_id INT NOT NULL GENERATED ALWAYS AS IDENTITY
            (START WITH 1, INCREMENT BY 1),
          user_id INT NOT NULL,
          mood_type VARCHAR(50),
          intensity INT,
          note VARCHAR(255),
          entry_date DATE,
          PRIMARY KEY (mood_id)
        )
      """.execute.apply()
    }

  def getLatest(userId: Int): Option[MoodModel] =
    DB readOnly { implicit s =>
      sql"""
        SELECT * FROM MOOD
        WHERE user_id = $userId
        ORDER BY entry_date DESC, mood_id DESC
        FETCH FIRST ROW ONLY
      """
        .map { rs =>
          new MoodModel(
            rs.int("user_id"),
            rs.string("mood_type"),
            rs.int("intensity"),
            rs.string("note"),
            rs.date("entry_date").toString
          )
        }
        .single
        .apply()
    }

  def getAll(userId: Int): List[MoodModel] =
    DB readOnly { implicit s =>
      sql"""
        SELECT * FROM MOOD
        WHERE user_id = $userId
        ORDER BY entry_date DESC, mood_id DESC
      """
        .map { rs =>
          new MoodModel(
            rs.int("user_id"),
            rs.string("mood_type"),
            rs.int("intensity"),
            rs.string("note"),
            rs.date("entry_date").toString
          )
        }
        .list
        .apply()
    }

  def countByType(userId: Int, mood: String): Int =
    DB readOnly { implicit s =>
      sql"""
        SELECT COUNT(*) FROM MOOD
        WHERE user_id = $userId
          AND mood_type = $mood
      """
        .map(_.int(1))
        .single
        .apply()
        .getOrElse(0)
    }

  def streakDays(userId: Int): Int =
    DB readOnly { implicit s =>
      sql"""
        SELECT COUNT(DISTINCT entry_date)
        FROM MOOD
        WHERE user_id = $userId
          AND entry_date >= CURRENT_DATE - 7 DAYS
      """
        .map(_.int(1))
        .single
        .apply()
        .getOrElse(0)
    }
}
