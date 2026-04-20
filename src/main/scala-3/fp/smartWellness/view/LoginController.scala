package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.{TextField, PasswordField, Label, Button, ProgressIndicator}
import scalafx.Includes._
import fp.smartWellness.model.UserModel
import fp.smartWellness.util.SessionManager
import fp.smartWellness.MainApp

@FXML
class LoginController:

  @FXML private var emailField: TextField = null
  @FXML private var passwordField: PasswordField = null
  @FXML private var messageLabel: Label = null
  @FXML private var loginButton: Button = null
  @FXML private var spinner: ProgressIndicator = null

  @FXML
  def handleSignUp(): Unit =
    MainApp.showSignup()

  @FXML
  def handleLogin(): Unit =
    // Reset UI
    messageLabel.text = ""
    spinner.visible = true
    loginButton.disable = true

    val email = emailField.text.value.trim
    val pw    = passwordField.text.value.trim

    if email.isEmpty then
      showError("Please enter your email.")
      return

    if pw.isEmpty then
      showError("Please enter your password.")
      return

    val userIdOpt = UserModel.getUserByEmailAndPassword(email, pw)

    userIdOpt match
      case Some(id) =>
        SessionManager.login(id)
        MainApp.showDashboard()

      case None =>
        showError("Invalid email or password.")

  private def showError(msg: String): Unit =
    messageLabel.text = msg
    spinner.visible = false
    loginButton.disable = false
