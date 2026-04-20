package fp.smartWellness.model

import scalafx.beans.property.*
import scalikejdbc.*
import fp.smartWellness.util.Database
import scala.util.Try
import java.time.{LocalDate, Period}
import org.mindrot.jbcrypt.BCrypt
import java.sql.Date

class UserModel(
                 val firstNameS: String,
                 val lastNameS: String,
                 val emailS: String,
                 passwordS: String,              // 👈 do NOT expose as val
                 val birthdayD: LocalDate,
                 val genderS: String,
                 val heightI: Int,
                 val weightI: Int
               ) extends Database {

  // ================= BASIC INFO =================
  var firstName = StringProperty(firstNameS)
  var lastName  = StringProperty(lastNameS)
  var email     = StringProperty(emailS)

  // 🔐 PASSWORD (HASHED ONLY)
  private var passwordHash = StringProperty(
    BCrypt.hashpw(passwordS, BCrypt.gensalt())
  )

  var birthday = ObjectProperty[LocalDate](birthdayD)
  var gender   = StringProperty(genderS)
  var height   = IntegerProperty(heightI)
  var weight   = IntegerProperty(weightI)

  var createdAt = ObjectProperty[LocalDate](LocalDate.now)

  // ================= DERIVED =================
  def age: Int =
    Period.between(birthday.value, LocalDate.now).getYears

  // ================= PASSWORD API =================

  /** Verify password (used by login & change password) */
  def verifyPassword(plainPw: String): Boolean =
    BCrypt.checkpw(plainPw, passwordHash.value)

  /** Update password securely */
  def updatePassword(newPassword: String): Unit = {
    passwordHash.value = BCrypt.hashpw(newPassword, BCrypt.gensalt())

    DB autoCommit { implicit s =>
      sql"""
        UPDATE USERS
        SET password = ${passwordHash.value}
        WHERE email = ${email.value}
      """.update.apply()
    }
  }

  // ================= CHECK EXISTENCE =================
  def isExist: Boolean =
    DB readOnly { implicit s =>
      sql"""
        SELECT 1 FROM USERS
        WHERE email = ${email.value}
      """
        .map(_.int(1))
        .single
        .apply()
        .isDefined
    }

  // ================= SIGN UP =================
  def save(): Try[Int] =
    if isExist then
      Try(throw new Exception("Email already exists"))
    else
      Try {
        DB autoCommit { implicit s =>
          sql"""
            INSERT INTO USERS (
              first_name, last_name, email, password,
              birthday, age, gender, height, weight, created_at
            ) VALUES (
              ${firstName.value},
              ${lastName.value},
              ${email.value},
              ${passwordHash.value},
              ${Date.valueOf(birthday.value)},
              ${age},
              ${gender.value},
              ${height.value},
              ${weight.value},
              ${Date.valueOf(createdAt.value)}
            )
          """.update.apply()
        }
      }

  // ================= UPDATE PROFILE =================
  def updateProfile(): Try[Int] =
    Try {
      DB autoCommit { implicit s =>
        sql"""
          UPDATE USERS
          SET height = ${height.value},
              weight = ${weight.value}
          WHERE email = ${email.value}
        """.update.apply()
      }
    }
}

// =======================================================
// ===================== COMPANION =======================
// =======================================================

object UserModel extends Database {

  def initializeTable(): Unit =
    DB autoCommit { implicit s =>
      sql"""
        CREATE TABLE USERS (
          user_id INT NOT NULL GENERATED ALWAYS AS IDENTITY
            (START WITH 1, INCREMENT BY 1),
          first_name VARCHAR(255),
          last_name  VARCHAR(255),
          email      VARCHAR(255),
          password   VARCHAR(255),
          birthday   DATE,
          age        INT,
          gender     VARCHAR(50),
          height     INT,
          weight     INT,
          created_at DATE,
          PRIMARY KEY (user_id)
        )
      """.execute.apply()
    }

  // ================= LOGIN =================
  def getUserByEmailAndPassword(emailStr: String, plainPw: String): Option[Int] =
    DB readOnly { implicit s =>
      sql"""
        SELECT user_id, password
        FROM USERS
        WHERE email = $emailStr
      """
        .map { rs =>
          if BCrypt.checkpw(plainPw, rs.string("password")) then
            Some(rs.int("user_id"))
          else None
        }
        .single
        .apply()
        .flatten
    }

  // ================= LOAD USER =================
  def getUserById(id: Int): Option[UserModel] =
    DB readOnly { implicit s =>
      sql"""
        SELECT * FROM USERS
        WHERE user_id = $id
      """
        .map { rs =>
          new UserModel(
            rs.string("first_name"),
            rs.string("last_name"),
            rs.string("email"),
            "DUMMY",                    // 👈 never expose real password
            rs.date("birthday").toLocalDate,
            rs.string("gender"),
            rs.int("height"),
            rs.int("weight")
          )
        }
        .single
        .apply()
    }
}
