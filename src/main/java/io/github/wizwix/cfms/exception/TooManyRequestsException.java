package io.github.wizwix.cfms.exception;

public class TooManyRequestsException extends RuntimeException {
  public TooManyRequestsException() {
    this("Too many requests");
  }

  public TooManyRequestsException(String message) {
    super(message);
  }
}
