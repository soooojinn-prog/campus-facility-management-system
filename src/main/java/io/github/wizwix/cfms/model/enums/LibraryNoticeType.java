package io.github.wizwix.cfms.model.enums;

public enum LibraryNoticeType {
  EVENT("이벤트"),
  INFO("안내"),
  NOTICE("공지"),
  URGENT("긴급");

  private final String displayName;

  LibraryNoticeType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
