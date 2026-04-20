package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.control.Label
import fp.smartWellness.model.{MoodModel, SleepModel}
import fp.smartWellness.util.SessionManager
import fp.smartWellness.MainApp
import scalafx.Includes.*

import java.time.LocalDate

@FXML
class StressLevelController {

  @FXML private var stressLevelLabel: Label = null
  @FXML private var reasonLabel: Label = null
  @FXML private var suggestionLabel: Label = null

  @FXML
  def initialize(): Unit = {

    if (!SessionManager.isLoggedIn) {
      stressLevelLabel.text = "Stress Level: --"
      reasonLabel.text = "Please log in to view your stress level."
      suggestionLabel.text = ""
      return
    }

    val userId = SessionManager.currentUserId.get
    val today  = LocalDate.now.toString

    val todayMoods =
      MoodModel.getAll(userId).filter(_.entryDate.value == today)

    val todaySleep =
      SleepModel.getAll(userId).find(_.entryDate.value == today)

    if (todayMoods.isEmpty && todaySleep.isEmpty) {
      stressLevelLabel.text = "Stress Level: --"
      reasonLabel.text = "No data recorded for today."
      suggestionLabel.text = "Log today’s mood and sleep to view your stress level."
      return
    }

    evaluateDailyStress(todayMoods, todaySleep)
  }

  // DAILY STRESS LOGIC
  private def evaluateDailyStress(
                                   moods: List[MoodModel],
                                   sleepOpt: Option[SleepModel]
                                 ): Unit = {

    // Mood stress calculation
    val moodStress =
      if (moods.nonEmpty) {
        val scores = moods.map { m =>
          val weight = m.moodType.value match {
            case "happy"   => 0.0
            case "neutral" => 0.2
            case "sad"     => 0.6
            case "anxiety" => 0.8
            case "angry"   => 0.7
            case _         => 0.3
          }
          weight * (m.intensity.value.toDouble / 5.0)
        }
        scores.sum / scores.size
      } else 0.0

    // Sleep stress calculation
    val sleepStress = sleepOpt match {
      case Some(s) =>
        math.min(1.0, math.abs(s.hours.value - 7.5) / 7.5)
      case None =>
        0.3 // mild uncertainty penalty
    }

    // Final stress index
    val stressIndex = (moodStress * 0.6) + (sleepStress * 0.4)

    // Interpret result
    if (stressIndex >= 0.65) {
      highStress(stressIndex)
    }
    else if (stressIndex >= 0.35) {
      moderateStress(stressIndex)
    }
    else {
      lowStress(stressIndex)
    }
  }


  private def highStress(score: Double): Unit = {
    stressLevelLabel.text = "Stress Level: HIGH 🔴"
    reasonLabel.text = f"Based on today’s mood and sleep data, your stress index is ${score}%.2f."
    suggestionLabel.text = "Consider resting, reducing workload, and practicing relaxation techniques."
  }

  private def moderateStress(score: Double): Unit = {
    stressLevelLabel.text = "Stress Level: MODERATE 🟠"
    reasonLabel.text = f"Some stress indicators were detected today (index: ${score}%.2f)."
    suggestionLabel.text = "Maintain healthy habits and monitor your stress level."
  }

  private def lowStress(score: Double): Unit = {
    stressLevelLabel.text = "Stress Level: LOW 🟢"
    reasonLabel.text = f"Your mood and sleep patterns today indicate low stress (index: ${score}%.2f)."
    suggestionLabel.text = "Great job! Keep maintaining your wellness routine."
  }


  @FXML
  def goDashboard(): Unit =
    MainApp.showDashboard()
}
