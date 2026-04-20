package fp.smartWellness.model

import scalafx.beans.property.*
import scalikejdbc.*
import fp.smartWellness.util.Database
import fp.smartWellness.badges.BadgeSystem
import scala.util.Try
import java.sql.Date

class SleepModel(
                  val userIdI: Int,
                  val sleepTimeS: String,
                  val wakeTimeS: String,
                  val hoursD: Double,
                  val qualityS: String,
                  val dateS: String,
                  val noteS: String
                ) extends Database {

  var userId    = IntegerProperty(userIdI)
  var sleepTime = StringProperty(sleepTimeS)
  var wakeTime  = StringProperty(wakeTimeS)
  var hours     = DoubleProperty(hoursD)
  var quality   = StringProperty(qualityS)
  var entryDate = StringProperty(dateS)
  var note      = StringProperty(noteS)

  // ===== INSERT =====
  def save(): Try[Int] =
    Try {
      val rowsInserted =
        DB autoCommit { implicit s =>
          sql"""
            INSERT INTO SLEEP (
              user_id,
              sleep_time,
              wake_time,
              hours,
              quality,
              entry_date,
              note
            )
            VALUES (
              ${userId.value},
              ${sleepTime.value},
              ${wakeTime.value},
              ${hours.value},
              ${quality.value},
              ${Date.valueOf(entryDate.value)},
              ${note.value}
            )
          """.update.apply()
        }

      // 🔔 Badge evaluation AFTER successful insert
      BadgeSystem.evaluate(
        userId.value,
        MoodModel.getAll(userId.value),
        SleepModel.getAll(userId.value)
      )

      // ✅ REQUIRED: return Int
      rowsInserted
    }

  // ===== UPDATE (BY USER + DATE) =====
  def update(): Unit =
    DB autoCommit { implicit s =>
      sql"""
        UPDATE SLEEP
        SET
          sleep_time = ${sleepTime.value},
          wake_time  = ${wakeTime.value},
          hours      = ${hours.value},
          quality    = ${quality.value},
          note       = ${note.value}
        WHERE user_id = ${userId.value}
          AND entry_date = ${Date.valueOf(entryDate.value)}
      """.update.apply()
    }
}

object SleepModel extends Database {

  // ===== TABLE INITIALIZATION =====
  def initializeTable(): Unit =
    DB autoCommit { implicit s =>
      sql"""
        CREATE TABLE SLEEP (
          sleep_id INT NOT NULL GENERATED ALWAYS AS IDENTITY
            (START WITH 1, INCREMENT BY 1),
          user_id INT NOT NULL,
          sleep_time VARCHAR(10),
          wake_time VARCHAR(10),
          hours DOUBLE,
          quality VARCHAR(50),
          entry_date DATE,
          note VARCHAR(255),
          PRIMARY KEY (sleep_id)
        )
      """.execute.apply()
    }

  // ===== GET ALL =====
  def getAll(userId: Int): List[SleepModel] =
    DB readOnly { implicit s =>
      sql"""
        SELECT * FROM SLEEP
        WHERE user_id = $userId
        ORDER BY entry_date DESC
      """
        .map { rs =>
          new SleepModel(
            rs.int("user_id"),
            rs.string("sleep_time"),
            rs.string("wake_time"),
            rs.double("hours"),
            rs.string("quality"),
            rs.date("entry_date").toString,
            rs.string("note")
          )
        }
        .list
        .apply()
    }

  // ===== GET LATEST =====
  def getLatest(userId: Int): Option[SleepModel] =
    DB readOnly { implicit s =>
      sql"""
        SELECT * FROM SLEEP
        WHERE user_id = $userId
        ORDER BY entry_date DESC
        FETCH FIRST ROW ONLY
      """
        .map { rs =>
          new SleepModel(
            rs.int("user_id"),
            rs.string("sleep_time"),
            rs.string("wake_time"),
            rs.double("hours"),
            rs.string("quality"),
            rs.date("entry_date").toString,
            rs.string("note")
          )
        }
        .single
        .apply()
    }
}
