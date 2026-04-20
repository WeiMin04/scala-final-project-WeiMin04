package fp.smartWellness.view

import javafx.fxml.FXML
import fp.smartWellness.MainApp

@FXML
class WelcomeController:
  @FXML
  def handleGetStarted(): Unit =
    MainApp.showSignup()

  @FXML
  def handleLogin(): Unit =
    MainApp.showLogin()
