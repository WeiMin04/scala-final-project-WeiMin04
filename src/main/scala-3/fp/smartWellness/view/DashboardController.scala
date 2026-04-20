package fp.smartWellness.view

import javafx.fxml.FXML
import javafx.scene.chart.{BarChart, XYChart}
import javafx.scene.control.{ChoiceBox, Label}
import fp.smartWellness.model.{MoodModel, SleepModel, UserModel}
import fp.smartWellness.util.SessionManager
import fp.smartWellness.MainApp
import scalafx.Includes.*
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.ButtonType

import java.time.LocalDate
import scala.math.sqrt

@FXML
class DashboardController {

  @FXML private var monthChoice: ChoiceBox[String] = null
  @FXML private var yearChoice: ChoiceBox[Int] = null

  @FXML private var moodChart: BarChart[String, Number] = null
  @FXML private var sleepChart: BarChart[String, Number] = null
  @FXML private var correlationLabel: Label = null
  @FXML private var wellnessScoreLabel: Label = null
  @FXML private var greetingLabel: Label = null
  @FXML private var moodSummaryLabel: Label = null
  @FXML private var sleepSummaryLabel: Label = null

  @FXML
  def initialize(): Unit = {
    if (!SessionManager.isLoggedIn) return

    val userId = SessionManager.currentUserId.get
    UserModel.getUserById(userId).foreach { u =>
      greetingLabel.text = s"Welcome back, ${u.firstName.value}"
    }

    // Month list
    monthChoice.getItems.addAll(
      "January","February","March","April","May","June",
      "July","August","September","October","November","December"
    )

    // Year list (current year ± 3)
    val currentYear = LocalDate.now.getYear
    yearChoice.getItems.addAll(
      (currentYear - 3) to (currentYear + 3): _*
    )

    monthChoice.getSelectionModel.select(LocalDate.now.getMonthValue - 1)
    yearChoice.getSelectionModel.select(Integer.valueOf(currentYear))

    loadDashboard()

    monthChoice.onAction = _ => loadDashboard()
    yearChoice.getSelectionModel.selectedItemProperty.addListener { (_, _, _) =>
      loadDashboard()
    }
  }

  private def loadDashboard(): Unit = {
    val userId = SessionManager.currentUserId.get
    val month  = monthChoice.getSelectionModel.getSelectedIndex + 1
    val year   = yearChoice.getValue

    val moods = MoodModel.getAll(userId).filter { m =>
      val d = LocalDate.parse(m.entryDate.value)
      d.getMonthValue == month && d.getYear == year
    }

    val sleeps = SleepModel.getAll(userId).filter { s =>
      val d = LocalDate.parse(s.entryDate.value)
      d.getMonthValue == month && d.getYear == year
    }

    loadMoodChart(moods)
    loadSleepChart(sleeps)
    computeCorrelation(moods, sleeps)
    computeWellnessScore(moods, sleeps)
    updateSummaries(moods, sleeps)
  }

  // MOOD CHART  
  private def loadMoodChart(moods: List[MoodModel]): Unit = {
    moodChart.getData.clear()
    val series = new XYChart.Series[String, Number]()

    moods.groupBy(_.moodType.value).foreach { case (mood, list) =>
      val data = new XYChart.Data[String, Number](mood.capitalize, list.size)
      series.getData.add(data)

      data.nodeProperty.addListener((_, _, node) => {
        if (node != null) {
          val color = mood match {
            case "happy"   => "#FFD4B8"
            case "neutral" => "#E5E9EC"
            case "sad"     => "#A9C8E3"
            case "anxiety" => "#E5A4A4"
            case "angry"   => "#FFB3A7"
            case _         => "#CCCCCC"
          }
          node.style = s"-fx-bar-fill: $color;"
        }
      })
    }

    moodChart.getData.add(series)
  }

  // SLEEP CHART  
  private def loadSleepChart(sleeps: List[SleepModel]): Unit = {
    sleepChart.getData.clear()
    val series = new XYChart.Series[String, Number]()
    series.name = "Sleep Hours"

    sleeps.sortBy(_.entryDate.value).foreach { s =>
      series.getData.add(
        new XYChart.Data[String, Number](s.entryDate.value, s.hours.value)
      )
    }

    sleepChart.getData.add(series)
  }

  //   SUMMARY  
  private def updateSummaries(moods: List[MoodModel], sleeps: List[SleepModel]): Unit = {

    moodSummaryLabel.text =
      if (moods.nonEmpty)
        moods.groupBy(_.moodType.value).map {
          case (m, l) => s"${l.size} ${m.capitalize}"
        }.mkString("You logged: ", ", ", ".")
      else
        "No mood data recorded for this period."

    sleepSummaryLabel.text =
      if (sleeps.nonEmpty) {
        val avg = sleeps.map(_.hours.value).sum / sleeps.size
        f"Average sleep duration: $avg%.1f hours per night."
      } else
        "No sleep data recorded for this period."
  }

  private def computeCorrelation(
                                  moods: List[MoodModel],
                                  sleeps: List[SleepModel]
                                ): Unit = {

    val paired = moods.flatMap { m =>
      sleeps.find(_.entryDate.value == m.entryDate.value)
        .map(s => (m.intensity.value.toDouble, s.hours.value))
    }

    // Not enough data points
    if (paired.size < 2) {
      correlationLabel.text = "Insufficient data for correlation."
      return
    }

    val (xs, ys) = paired.unzip
    val mx = xs.sum / xs.size
    val my = ys.sum / ys.size

    val num = xs.zip(ys).map { case (x, y) =>
      (x - mx) * (y - my)
    }.sum

    val dx = xs.map(x => math.pow(x - mx, 2)).sum
    val dy = ys.map(y => math.pow(y - my, 2)).sum

    // 🚨 Critical fix: zero variance check
    if (dx == 0 || dy == 0) {
      correlationLabel.text = "Correlation unavailable (no variation in data)."
      return
    }

    val den = sqrt(dx) * sqrt(dy)
    val corr = num / den

    correlationLabel.text = f"Mood ↔ Sleep Correlation: $corr%.2f"
  }


  private def computeWellnessScore(
                                    moods: List[MoodModel],
                                    sleeps: List[SleepModel]
                                  ): Unit = {

    // ===== Mood normalization =====
    val moodScore =
      if (moods.nonEmpty) {
        val scores = moods.map { m =>
          val base = m.moodType.value match {
            case "happy" => 1.0
            case "neutral" => 0.6
            case "sad" => 0.3
            case "anxiety" => 0.25
            case "angry" => 0.2
            case _ => 0.5
          }
          base * (m.intensity.value.toDouble / 5.0)
        }
        scores.sum / scores.size
      } else 0.0

    // ===== Sleep normalization =====
    val sleepScore =
      if (sleeps.nonEmpty) {
        val scores = sleeps.map { s =>
          val deviation = math.abs(s.hours.value - 7.5)
          math.max(0.0, 1.0 - (deviation / 7.5))
        }
        scores.sum / scores.size
      } else 0.0

    // ===== Final wellness score =====
    val finalScore = ((moodScore * 0.5) + (sleepScore * 0.5)) * 100

    wellnessScoreLabel.text =
      f"Wellness Score: ${finalScore.toInt} / 100"
  }


  //   NAVIGATION  
  @FXML def goMood(): Unit = MainApp.showMood()
  @FXML def goSleep(): Unit = MainApp.showSleep()
  @FXML def goHistory(): Unit = MainApp.showHistory()
  @FXML def goStress(): Unit = MainApp.showStress()
  @FXML def goAchievement(): Unit = MainApp.showAchievement()
  @FXML def goProfile(): Unit = MainApp.showProfile()

  //   LOGOUT  
  @FXML
  def handleLogout(): Unit =
    new Alert(AlertType.Confirmation) {
      title = "Logout"
      headerText = "Confirm Logout"
      contentText = "Are you sure you want to log out?"
    }.showAndWait() match {
      case Some(ButtonType.OK) =>
        SessionManager.logout()
        MainApp.showWelcome()
      case _ =>
    }
}
