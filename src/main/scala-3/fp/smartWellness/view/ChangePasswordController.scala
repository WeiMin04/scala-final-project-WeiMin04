package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.{PasswordField, Label}
import fp.smartWellness.model.UserModel
import fp.smartWellness.util.SessionManager
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.Includes.*

@FXML
class ChangePasswordController {

  @FXML private var newPasswordField: PasswordField = null
  @FXML private var confirmPasswordField: PasswordField = null
  @FXML private var messageLabel: Label = null

  // UPDATE PASSWORD

  @FXML
  def handleChangePassword(): Unit = {

    val newPw     = newPasswordField.getText
    val confirmPw = confirmPasswordField.getText

    if (newPw.isEmpty || confirmPw.isEmpty) {
      messageLabel.text = "All fields are required."
      return
    }

    if (newPw.length < 6) {
      messageLabel.text = "Password must be at least 6 characters."
      return
    }

    if (newPw != confirmPw) {
      messageLabel.text = "Passwords do not match."
      return
    }

    val userId = SessionManager.currentUserId.get
    val user = UserModel.getUserById(userId).get

    user.updatePassword(newPw)

    new Alert(AlertType.Information) {
      title = "Password Updated"
      headerText = "Success"
      contentText = "Your password has been updated successfully."
    }.showAndWait()

    handleClose()
  }

  @FXML
  def handleClose(): Unit =
    newPasswordField.getScene.getWindow.hide()
}
