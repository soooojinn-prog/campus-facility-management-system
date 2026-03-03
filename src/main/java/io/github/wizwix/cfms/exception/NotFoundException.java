package io.github.wizwix.cfms.exception;

public class NotFoundException extends RuntimeException {
  public NotFoundException() {
    this("Not Found");
  }

  public NotFoundException(String message) {
    super(message);
  }
}
