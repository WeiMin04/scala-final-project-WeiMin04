package fp.smartWellness.badges

import fp.smartWellness.model.{AchievementModel, MoodModel, SleepModel}

import java.time.LocalDate
import scalikejdbc.AutoSession

object BadgeSystem {

  private val allBadges: List[Badge] = List(
    new SleepHero,
    new ConsistentLogger,
    new StressFighter
  )

  def evaluate(
                userId: Int,
                moods: List[MoodModel],
                sleeps: List[SleepModel]
              ): Unit = {

    val today = LocalDate.now.toString

    allBadges.foreach { badge =>
      if (badge.isUnlocked(moods, sleeps)) {
        AchievementModel.awardOnce(
          userId   = userId,
          badge    = badge.name,        // stored as badge_name
          desc     = badge.description, // stored as description
          date     = today
        )(using AutoSession)
      }
    }
  }
}
