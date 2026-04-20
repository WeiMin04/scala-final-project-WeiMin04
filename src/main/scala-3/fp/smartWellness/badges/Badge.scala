package fp.smartWellness.badges

import fp.smartWellness.model.{MoodModel, SleepModel}

trait Badge {

  def id: String
  def name: String
  def description: String

  /**
   * Return true if user qualifies for this badge
   */
  def isUnlocked(
                  moods: List[MoodModel],
                  sleeps: List[SleepModel]
                ): Boolean
}
