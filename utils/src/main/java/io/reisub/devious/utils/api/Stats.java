package io.reisub.devious.utils.api;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

public class Stats {
  public static String getFormattedDurationBetween(Instant start, Instant finish) {
    return getFormattedDuration(Duration.between(start, finish));
  }

  public static String getFormattedDuration(Duration duration) {
    return String.format(
        "%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  public static long getExperiencePerHour(int experienceGained, Instant start) {
    long seconds = Duration.between(start, Instant.now()).toSeconds();
    seconds = Math.max(1, seconds);
    return experienceGained * 3600L / seconds;
  }

  public static long getExperiencePerHourInThousands(int experienceGained, Instant start) {
    return getExperiencePerHour(experienceGained, start) / 1000;
  }

  public static String getFormattedExperience(int experience) {
    return getFormattedExperience((long) experience);
  }

  public static String getFormattedExperience(long experience) {
    DecimalFormat formatter = new DecimalFormat("#,###");
    return formatter.format(experience);
  }

  public static String getFormattedExperiencePerHour(int experienceGained, Instant start) {
    return getFormattedExperience(getExperiencePerHour(experienceGained, start));
  }
}
