package io.reisub.devious.utils.tasks;

import io.reisub.devious.utils.api.Activity;

public abstract class Task {

  public Activity getActivity() {
    return Activity.IDLE;
  }

  public abstract String getStatus();

  public abstract boolean validate();

  public abstract void execute();
}
