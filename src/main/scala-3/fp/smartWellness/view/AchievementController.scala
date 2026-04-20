package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.collections.FXCollections
import fp.smartWellness.model.AchievementModel
import fp.smartWellness.util.SessionManager
import scalafx.Includes.*

@FXML
class AchievementController {

  @FXML private var badgeList: ListView[String] = null

  @FXML
  def initialize(): Unit = {

    if (!SessionManager.isLoggedIn) {
      badgeList.items = FXCollections.observableArrayList("Please log in to view achievements.")
      return
    }

    val userId = SessionManager.currentUserId.get
    val achievements = AchievementModel.getAll(userId)

    if (achievements.nonEmpty) {

      val items = achievements.map { a =>
        s"🏆 ${a.badgeName.value}\n${a.description.value}"
      }

      badgeList.items = FXCollections.observableArrayList(items: _*)
      

    } else {

      badgeList.items = FXCollections.observableArrayList("No achievements yet.\nTrack your mood and sleep consistently to unlock rewards!")
    }
  }
}
