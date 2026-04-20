package fp.smartWellness.badges

import fp.smartWellness.model.{MoodModel, SleepModel}

class ConsistentLogger extends Badge {

  override val id: String = "CONSISTENT_LOGGER"
  override val name: String = "Consistent Logger 📅"
  override val description: String =
    "Logged your mood for 7 different days."

  override def isUnlocked(
                           moods: List[MoodModel],
                           sleeps: List[SleepModel]
                         ): Boolean = {

    moods.map(_.entryDate.value).distinct.size >= 7
  }
}
