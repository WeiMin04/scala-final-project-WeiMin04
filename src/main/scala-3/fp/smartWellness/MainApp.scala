package fp.smartWellness

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene as jfxs
import scalafx.scene.image.Image
import scalafx.Includes.*
import fp.smartWellness.util.{Database, SessionManager}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonType
import scalafx.stage.{Modality, Stage}

object MainApp extends JFXApp3:
  private var rootLayout: Option[jfxs.layout.BorderPane] = None
  private val css = getClass.getResource("view/style.css")

  override def start(): Unit =
    Database.setupDB()

    stage = new PrimaryStage():
      title = "BloomWell"
      icons += new Image(getClass.getResourceAsStream("/images/Logo.png"))
      scene = new Scene(1200, 800):
        stylesheets = Seq(css.toExternalForm)

    showWelcome()

  def showWelcome(): Unit =
    val loader = new FXMLLoader(getClass.getResource("view/Welcome.fxml"))
    loader.load()
    stage.scene().root = loader.getRoot[jfxs.Parent]

  def showLogin(): Unit =
    val loader = new FXMLLoader(getClass.getResource("view/LoginView.fxml"))
    loader.load()
    stage.scene().root = loader.getRoot[jfxs.Parent]

  def showSignup(): Unit =
    val loader = new FXMLLoader(getClass.getResource("view/SignUpView.fxml"))
    loader.load()
    stage.scene().root = loader.getRoot[jfxs.Parent]

  def showChangePassword(): Unit =
    val resource = getClass.getResource("view/ChangePasswordView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()

    val pane = loader.getRoot[jfxs.Parent]

    val stage = new javafx.stage.Stage()
    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL)
    stage.title = "Change Password"
    stage.scene = new Scene(pane)
    stage.showAndWait()

  def showDashboard(): Unit =
    // Load RootLayout first
    val rootLoader = new FXMLLoader(getClass.getResource("view/RootLayout.fxml"))
    rootLoader.load()
    rootLayout = Some(rootLoader.getRoot[jfxs.layout.BorderPane])

    stage.scene().root = rootLayout.get

    // Load Dashboard into center
    val pageLoader = new FXMLLoader(getClass.getResource("view/DashboardView.fxml"))
    pageLoader.load()
    rootLayout.foreach(_.center = pageLoader.getRoot[jfxs.Parent])

  def showMood(): Unit =
    guard()
    loadCenter("view/MoodView.fxml")

  def showSleep(): Unit =
    guard()
    loadCenter("view/SleepView.fxml")

  def showProfile(): Unit =
    guard()
    loadCenter("view/ProfileView.fxml")


  def showHistory(): Unit =
    guard()
    val resource = getClass.getResource("view/HistoryView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val pane = loader.getRoot[jfxs.Parent]
    rootLayout.foreach(_.center = pane)

  def showStress(): Unit =
    guard()
    val resource = getClass.getResource("view/StressLevelView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val pane = loader.getRoot[jfxs.Parent]
    rootLayout.foreach(_.center = pane)

  def showAchievement(): Unit =
    guard()

    val resource = getClass.getResource("view/AchievementView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()

    val root = loader.getRoot[jfxs.Parent]

    val popupStage = new Stage():
      title = "Achievements"
      scene = new Scene(root, 400, 400)
      initOwner(stage)
      initModality(Modality.ApplicationModal)
      resizable = false

    popupStage.showAndWait()

  def confirmLogout(): Unit =
    val alert = new Alert(Alert.AlertType.Confirmation)
    alert.title = "Confirm Logout"
    alert.headerText = "Are you sure you want to log out?"
    alert.contentText = "Any unsaved changes will be lost."

    alert.showAndWait().foreach { btn =>
      if btn == ButtonType.OK then
        SessionManager.logout()
        rootLayout = None
        showWelcome()
    }

  def showAchievementPopup(badgeName: String, description: String): Unit =
    new Alert(AlertType.Information) {
      title = "New Achievement!"
      headerText = badgeName
      contentText = description
      buttonTypes = Seq(ButtonType.OK)
    }.showAndWait()

  private def guard(): Unit =
    if !SessionManager.isLoggedIn then
      showLogin()
      throw new IllegalStateException("User not logged in")

  private def loadCenter(fxml: String): Unit =
    val loader = new FXMLLoader(getClass.getResource(fxml))
    loader.load()
    rootLayout.foreach(_.center = loader.getRoot[jfxs.Parent])

end MainApp

