package io.reisub.devious.utils.tasks;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentTask extends Task {
  protected final List<Task> children = new ArrayList<>();
  protected Task current;

  @Override
  public String getStatus() {
    if (current != null) {
      return current.getStatus();
    }

    return "";
  }

  public void addChildren(Task... children) {
    this.children.addAll(List.of(children));
  }

  @Override
  public boolean validate() {
    for (Task t : children) {
      if (t.validate()) {
        current = t;
        return true;
      }
    }

    return false;
  }

  @Override
  public void execute() {
    if (current != null) {
      current.execute();
      current = null;
    }
  }
}
