package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, TextField}
import javafx.scene.text.Text
import fp.smartWellness.model.UserModel
import fp.smartWellness.util.SessionManager
import fp.smartWellness.MainApp
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.Includes.*


import java.time.{LocalDate, Period}

@FXML
class ProfileController {

  //  READ-ONLY TEXT 
  @FXML private var firstNameText: Text = null
  @FXML private var lastNameText: Text = null
  @FXML private var genderText: Text = null
  @FXML private var birthdayText: Text = null
  @FXML private var ageText: Text = null

  //  EDITABLE 
  @FXML private var heightField: TextField = null
  @FXML private var weightField: TextField = null
  @FXML private var messageLabel: Label = null

  private var user: UserModel = null

  //  INITIALIZE 
  @FXML
  def initialize(): Unit = {
    val userId = SessionManager.currentUserId.get
    user = UserModel.getUserById(userId).get

    firstNameText.text = user.firstName.value
    lastNameText.text  = user.lastName.value
    genderText.text    = user.gender.value
    birthdayText.text  = user.birthday.value.toString

    val age = Period.between(user.birthday.value, LocalDate.now).getYears
    ageText.text = age.toString

    heightField.text = user.height.value.toString
    weightField.text = user.weight.value.toString
  }

  //  SAVE PROFILE 
  @FXML
  def saveProfile(): Unit = {

    val heightStr = heightField.text.value
    val weightStr = weightField.text.value

    if (!heightStr.forall(_.isDigit)) {
      messageLabel.text = "Height must be numeric."
      return
    }

    if (!weightStr.forall(_.isDigit)) {
      messageLabel.text = "Weight must be numeric."
      return
    }

    user.height.value = heightStr.toInt
    user.weight.value = weightStr.toInt
    user.updateProfile()

    new Alert(AlertType.Information) {
      title = "Profile Updated"
      headerText = "Profile updated successfully"
    }.showAndWait()

  }

  //  NAVIGATION 
  @FXML def handleChangePassword(): Unit = MainApp.showChangePassword()
  @FXML def handleLogout(): Unit = MainApp.confirmLogout()
  @FXML def goDashboard(): Unit = MainApp.showDashboard()
}
