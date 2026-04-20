package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.*
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.Includes.*
import fp.smartWellness.model.UserModel
import fp.smartWellness.MainApp

import java.time.{LocalDate, Period}

@FXML
class SignUpController:

  @FXML private var firstNameField: TextField = null
  @FXML private var lastNameField: TextField = null
  @FXML private var emailField: TextField = null
  @FXML private var passwordField: PasswordField = null
  @FXML private var confirmField: PasswordField = null
  @FXML private var genderChoice: ChoiceBox[String] = null
  @FXML private var birthdayPicker: DatePicker = null
  @FXML private var heightField: TextField = null
  @FXML private var weightField: TextField = null
  @FXML private var errorLabel: Label = null
  @FXML private var spinner: ProgressIndicator = null
  @FXML private var signUpButton: Button = null

  @FXML
  def initialize(): Unit =
    genderChoice.items = ObservableBuffer("Male", "Female", "Prefer Not to Say")
    errorLabel.visible = false
    errorLabel.managed = false
    spinner.visible = false

  @FXML
  def handleSignUp(): Unit =
    errorLabel.text = ""
    errorLabel.visible = false
    signUpButton.disable = true

    val first   = firstNameField.text.value.trim
    val last    = lastNameField.text.value.trim
    val email   = emailField.text.value.trim
    val pwd     = passwordField.text.value
    val confirm = confirmField.text.value
    val gender  = Option(genderChoice.value.value).orNull
    val birthday: LocalDate = birthdayPicker.value.value
    val heightText = heightField.text.value.trim
    val weightText = weightField.text.value.trim

    validate(first, last, email, pwd, confirm, gender, birthday, heightText, weightText) match
      case Some(err) =>
        showError(err)
        return
      case None => ()

    val user = new UserModel(
      first,
      last,
      email,
      pwd,
      birthday,
      gender,
      heightText.toInt,
      weightText.toInt
    )

    user.save().toEither match
      case Right(_) =>
        new Alert(AlertType.Information) {
          title = "Success"
          headerText = "Account Created"
          contentText = "Your account has been created successfully."
        }.showAndWait()

        MainApp.showLogin()

      case Left(ex) =>
        showError(ex.getMessage)

  @FXML
  def handleLogin(): Unit =
    MainApp.showLogin()

  @FXML
  def handleBack(): Unit =
    MainApp.showWelcome()

  private def validate(
                        first: String,
                        last: String,
                        email: String,
                        pwd: String,
                        confirm: String,
                        gender: String,
                        birthday: LocalDate,
                        height: String,
                        weight: String
                      ): Option[String] =
    if first.isEmpty then Some("First name is required.")
    else if last.isEmpty then Some("Last name is required.")
    else if email.isEmpty || !email.contains("@") then Some("Invalid email.")
    else if pwd.length < 6 then Some("Password must be at least 6 characters.")
    else if pwd != confirm then Some("Passwords do not match.")
    else if gender == null then Some("Please select gender.")
    else if birthday == null then Some("Please choose your birthday.")
    else if birthday.isAfter(LocalDate.now) then Some("Birthday cannot be in the future.")
    else {
      val age = Period.between(birthday, LocalDate.now).getYears
      if age < 13 then
        Some("You must be at least 13 years old to use this application.")
      else if !height.forall(_.isDigit) then
        Some("Height must be numeric.")
      else if !weight.forall(_.isDigit) then
        Some("Weight must be numeric.")
      else None
    }

  private def showError(msg: String): Unit =
    errorLabel.text = msg
    errorLabel.visible = true
    errorLabel.managed = true
    signUpButton.disable = false
