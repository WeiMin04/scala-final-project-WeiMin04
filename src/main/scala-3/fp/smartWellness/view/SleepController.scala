package fp.smartWellness.view

import fp.smartWellness.MainApp
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, Label, TextArea, TextField, DatePicker}
import fp.smartWellness.model.SleepModel
import fp.smartWellness.util.SessionManager
import fp.smartWellness.util.Database.given
import scalafx.Includes.*

import java.time.{Duration, LocalDate, LocalTime}

@FXML
class SleepController {

  @FXML private var sleepTimeField: TextField = null
  @FXML private var wakeTimeField: TextField = null
  @FXML private var qualityChoice: ChoiceBox[String] = null
  @FXML private var symptomArea: TextArea = null
  @FXML private var messageLabel: Label = null
  @FXML private var datePicker: DatePicker = null

  // ================= INITIALIZE =================
  @FXML
  def initialize(): Unit = {
    qualityChoice.getItems.addAll(
      "Very Poor", "Poor", "Fair", "Good", "Excellent"
    )
    datePicker.value = LocalDate.now
    messageLabel.text = ""
  }

  // ================= SAVE SLEEP =================
  @FXML
  def saveSleep(): Unit = {

    if (!SessionManager.isLoggedIn) {
      messageLabel.text = "Please log in first."
      return
    }

    val date = datePicker.value.value
    if (date == null) {
      messageLabel.text = "Please select a date."
      return
    }

    val sleepText = sleepTimeField.text.value.trim
    val wakeText  = wakeTimeField.text.value.trim
    val quality   = qualityChoice.value

    if (sleepText.isEmpty || wakeText.isEmpty || quality == null) {
      messageLabel.text = "All fields are required."
      return
    }

    val sleepTimeOpt = parseTime(sleepText)
    val wakeTimeOpt  = parseTime(wakeText)

    if (sleepTimeOpt.isEmpty || wakeTimeOpt.isEmpty) {
      messageLabel.text = "Time must be HH:mm."
      return
    }

    val hoursSlept = calculateHours(sleepTimeOpt.get, wakeTimeOpt.get)
    if (hoursSlept <= 0) {
      messageLabel.text = "Invalid sleep duration."
      return
    }

    val userId  = SessionManager.currentUserId.get
    val note    = Option(symptomArea.text.value).getOrElse("")
    val sqlDate = java.sql.Date.valueOf(date)

    SleepModel.getAll(userId).find(_.entryDate.value == date.toString) match {

      case Some(existing) =>
        existing.sleepTime.value = sleepText
        existing.wakeTime.value  = wakeText
        existing.hours.value     = hoursSlept
        existing.quality.value   = quality.value
        existing.note.value      = note
        existing.update()
        messageLabel.text = "Sleep record updated."

      case None =>
        val result = new SleepModel(
          userId,
          sleepText,
          wakeText,
          hoursSlept,
          quality.value,
          sqlDate.toString,
          note
        ).save()

        if (result.isFailure) {
          messageLabel.text = "Database insert failed."
          result.failed.foreach(_.printStackTrace())
          return
        }

        messageLabel.text = f"Sleep recorded: $hoursSlept%.2f hours"
    }

    sleepTimeField.clear()
    wakeTimeField.clear()
    symptomArea.clear()
  }

  @FXML
  def goDashboard(): Unit =
    MainApp.showDashboard()

  // HELPERS
  private def parseTime(text: String): Option[LocalTime] =
    try Some(LocalTime.parse(text))
    catch { case _: Exception => None }

  private def calculateHours(start: LocalTime, end: LocalTime): Double = {
    val duration =
      if (end.isAfter(start)) Duration.between(start, end)
      else Duration.between(start, end).plusHours(24)

    duration.toMinutes / 60.0
  }
}
