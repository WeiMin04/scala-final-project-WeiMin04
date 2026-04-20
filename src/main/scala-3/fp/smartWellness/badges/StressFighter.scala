package fp.smartWellness.badges

import fp.smartWellness.model.{MoodModel, SleepModel}

class StressFighter extends Badge {

  override val id: String = "STRESS_FIGHTER"
  override val name: String = "Stress Fighter 💪"
  override val description: String =
    "Maintained emotional stability without anxiety or anger."

  override def isUnlocked(
                           moods: List[MoodModel],
                           sleeps: List[SleepModel]
                         ): Boolean = {

    val negative = Set("anxiety", "angry")
    moods.count(m => !negative.contains(m.moodType.value)) >= 5
  }
}
