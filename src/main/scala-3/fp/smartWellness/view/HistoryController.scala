package fp.smartWellness.view

import fp.smartWellness.MainApp
import javafx.fxml.FXML
import javafx.scene.control.{TableColumn, TableView}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.ObjectProperty
import fp.smartWellness.model.{MoodModel, SleepModel}
import fp.smartWellness.util.SessionManager
import scalafx.Includes.*

@FXML
class HistoryController {

  //  MOOD TABLE 
  @FXML private var moodTable: TableView[MoodModel] = null
  @FXML private var moodTypeCol: TableColumn[MoodModel, String] = null
  @FXML private var moodDateCol: TableColumn[MoodModel, String] = null

  //  SLEEP TABLE 
  @FXML private var sleepTable: TableView[SleepModel] = null
  @FXML private var sleepStartCol: TableColumn[SleepModel, String] = null
  @FXML private var sleepEndCol: TableColumn[SleepModel, String] = null
  @FXML private var sleepHoursCol: TableColumn[SleepModel, Number] = null
  @FXML private var sleepQualityCol: TableColumn[SleepModel, String] = null
  @FXML private var sleepDateCol: TableColumn[SleepModel, String] = null

  //  INITIALIZE 
  @FXML
  def initialize(): Unit = {

    if (!SessionManager.isLoggedIn) {
      moodTable.items = ObservableBuffer()
      sleepTable.items = ObservableBuffer()
      return
    }

    val userId = SessionManager.currentUserId.get

    //  Mood bindings 
    moodTypeCol.cellValueFactory = _.value.moodType
    moodDateCol.cellValueFactory = _.value.entryDate

    //  Sleep bindings (FIXED) 
    sleepStartCol.cellValueFactory = _.value.sleepTime
    sleepEndCol.cellValueFactory   = _.value.wakeTime

    sleepHoursCol.cellValueFactory =
      cell => ObjectProperty[Number](cell.value.hours.value)

    sleepQualityCol.cellValueFactory = _.value.quality
    sleepDateCol.cellValueFactory    = _.value.entryDate

    //  Load data 
    moodTable.items = ObservableBuffer(
      MoodModel.getAll(userId): _*
    )

    sleepTable.items = ObservableBuffer(
      SleepModel.getAll(userId): _*
    )
  }

  //  NAVIGATION 
  @FXML
  def goDashboard(): Unit =
    MainApp.showDashboard()
}
