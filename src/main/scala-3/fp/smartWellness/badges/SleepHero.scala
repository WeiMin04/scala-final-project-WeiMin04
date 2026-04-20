package fp.smartWellness.badges

import fp.smartWellness.model.{MoodModel, SleepModel}

class SleepHero extends Badge {

  override val id: String = "SLEEP_HERO"
  override val name: String = "Sleep Hero 🛌"
  override val description: String =
    "Slept at least 8 hours on 5 different days."

  override def isUnlocked(
                           moods: List[MoodModel],
                           sleeps: List[SleepModel]
                         ): Boolean = {

    sleeps.count(_.hours.value >= 8.0) >= 5
  }
}
