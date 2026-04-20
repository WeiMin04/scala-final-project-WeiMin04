package fp.smartWellness.view

import javafx.fxml.FXML
import fp.smartWellness.MainApp

@FXML
class RootLayoutController:

  @FXML def handleDashboard(): Unit =
    MainApp.showDashboard()

  @FXML def handleLogMood(): Unit =
    MainApp.showMood()

  @FXML def handleLogSleep(): Unit =
    MainApp.showSleep()

  @FXML def handleHistory(): Unit =
    MainApp.showHistory()

  @FXML def handleStress(): Unit =
    MainApp.showStress()

  @FXML def handleProfile(): Unit =
    MainApp.showProfile()

  @FXML def handleAchievement(): Unit =
    MainApp.showAchievement()

  @FXML def handleLogout(): Unit =
    MainApp.confirmLogout()
