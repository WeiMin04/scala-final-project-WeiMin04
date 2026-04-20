package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, TextArea, Button}
import fp.smartWellness.model.{MoodModel, AchievementModel}
import fp.smartWellness.util.SessionManager
import fp.smartWellness.MainApp
import scalafx.Includes.*

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import javafx.application.Platform

@FXML
class MoodController {

  // ===== FXML =====
  @FXML private var messageLabel: Label = null
  @FXML private var noteArea: TextArea = null
  @FXML private var dateLabel: Label = null

  @FXML private var happyBtn: Button = null
  @FXML private var neutralBtn: Button = null
  @FXML private var sadBtn: Button = null
  @FXML private var anxietyBtn: Button = null
  @FXML private var angryBtn: Button = null

  private var selectedMood: String = ""

  // ===== INITIALIZE =====
  @FXML
  def initialize(): Unit = {
    dateLabel.text = s"Today: ${LocalDate.now}"
    messageLabel.text = ""
  }

  // ===== MOOD SELECTION =====
  @FXML def selectHappy(): Unit   = selectMood("happy", happyBtn)
  @FXML def selectNeutral(): Unit = selectMood("neutral", neutralBtn)
  @FXML def selectSad(): Unit     = selectMood("sad", sadBtn)
  @FXML def selectAnxiety(): Unit = selectMood("anxiety", anxietyBtn)
  @FXML def selectAngry(): Unit   = selectMood("angry", angryBtn)

  private def selectMood(mood: String, btn: Button): Unit = {
    selectedMood = mood
    resetButtons()
    btn.style = "-fx-border-color: #372315; -fx-border-width: 3;"
  }

  private def resetButtons(): Unit =
    List(happyBtn, neutralBtn, sadBtn, anxietyBtn, angryBtn)
      .foreach(b => if (b != null) b.style = "")

  // ===== SAVE MOOD (ASYNC, NO FREEZE) =====
  @FXML
  def saveMood(): Unit = {

    if (!SessionManager.isLoggedIn) {
      messageLabel.text = "Please log in first."
      return
    }

    if (selectedMood.isEmpty) {
      messageLabel.text = "Please select a mood."
      return
    }

    // Disable UI to prevent double click
    setButtonsDisabled(true)
    messageLabel.text = "Saving mood..."

    val userId = SessionManager.currentUserId.get
    val today  = LocalDate.now.toString
    val note   = Option(noteArea.getText).getOrElse("")
    val mood   = selectedMood

    Future {
      new MoodModel(
        userId,
        mood,
        3,
        note,
        today
      ).save()

      if (
        mood == "happy" &&
          AchievementModel.awardOnce(
            userId,
            "Positive Vibes",
            "Logged a happy mood!",
            today
          )
      ) {
        Platform.runLater(() => {
          MainApp.showAchievementPopup(
            "Positive Vibes",
            "Logged your first happy mood!"
          )
        })
      }

    }.onComplete { _ =>
      Platform.runLater(() => {
        messageLabel.text = "Mood recorded successfully!"
        selectedMood = ""
        resetButtons()
        noteArea.clear()
        setButtonsDisabled(false)
      })
    }
  }

  private def setButtonsDisabled(disabled: Boolean): Unit =
    List(happyBtn, neutralBtn, sadBtn, anxietyBtn, angryBtn)
      .foreach(b => if (b != null) b.setDisable(disabled))

  @FXML
  def goDashboard(): Unit =
    MainApp.showDashboard()
}
