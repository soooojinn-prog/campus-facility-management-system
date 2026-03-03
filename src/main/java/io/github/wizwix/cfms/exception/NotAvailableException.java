package io.github.wizwix.cfms.exception;

public class NotAvailableException extends RuntimeException {
  public NotAvailableException() {
    this("Not available");
  }

  public NotAvailableException(String message) {
    super(message);
  }
}
