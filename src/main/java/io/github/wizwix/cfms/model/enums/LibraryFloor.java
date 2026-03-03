package io.github.wizwix.cfms.model.enums;

public enum LibraryFloor {
  B1("B1"),
  F1("1F"),
  F2("2F"),
  F3("3F"),
  F4("4F");

  private final String displayString;

  LibraryFloor(String displayString) {
    this.displayString = displayString;
  }

  /// Returns the floor string in the format the frontend expects (e.g. "2F", "B1")
  public String toDisplayString() {
    return displayString;
  }
}
