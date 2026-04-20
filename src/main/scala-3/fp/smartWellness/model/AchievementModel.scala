package fp.smartWellness.model

import scalafx.beans.property.*
import scalikejdbc.*
import fp.smartWellness.util.Database
import scala.util.Try

class AchievementModel(
                        val userIdI: Int,
                        val nameS: String,
                        val descriptionS: String,
                        val dateS: String
                      ) extends Database:

  // ScalaFX properties
  val userId      = IntegerProperty(userIdI)
  val badgeName   = StringProperty(nameS)
  val description = StringProperty(descriptionS)
  val dateEarned  = StringProperty(dateS)

  /** Insert achievement into DB */
  def save(): Try[Int] =
    Try {
      DB autoCommit { implicit session =>
        sql"""
          INSERT INTO ACHIEVEMENT (
            user_id, badge_name, description, date_earned
          ) VALUES (
            ${userId.value},
            ${badgeName.value},
            ${description.value},
            ${dateEarned.value}
          )
        """.update.apply()
      }
    }


object AchievementModel extends Database:

  /** Creates table on first run */
  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE ACHIEVEMENT (
          achievement_id INT NOT NULL GENERATED ALWAYS AS IDENTITY 
            (START WITH 1, INCREMENT BY 1),
          user_id INT NOT NULL,
          badge_name VARCHAR(255),
          description VARCHAR(255),
          date_earned VARCHAR(50),
          PRIMARY KEY (achievement_id)
        )
      """.execute.apply()
    }

  /** Returns all achievements for a user */
  def getAll(userId: Int): List[AchievementModel] =
    DB readOnly { implicit session =>
      sql"SELECT * FROM ACHIEVEMENT WHERE user_id = $userId"
        .map { rs =>
          new AchievementModel(
            rs.int("user_id"),
            rs.string("badge_name"),
            rs.string("description"),
            rs.string("date_earned")
          )
        }.list.apply()
    }

  /** Checks if an achievement already exists */
  def exists(userId: Int, name: String)(using session: DBSession = AutoSession): Boolean =
    sql"""
      SELECT COUNT(*) FROM ACHIEVEMENT
      WHERE user_id = $userId AND badge_name = $name
    """
      .map(_.int(1))
      .single
      .apply()
      .getOrElse(0) > 0

  def awardOnce(userId: Int, badge: String, desc: String, date: String)(using session: DBSession = AutoSession): Boolean =
    if !exists(userId, badge) then
      new AchievementModel(userId, badge, desc, date).save()
      true
    else false

