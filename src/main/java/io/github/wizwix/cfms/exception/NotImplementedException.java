package io.github.wizwix.cfms.exception;

public class NotImplementedException extends RuntimeException {
  public NotImplementedException() {
    this("Not Implemented");
  }

  public NotImplementedException(String message) {
    super(message);
  }
}
