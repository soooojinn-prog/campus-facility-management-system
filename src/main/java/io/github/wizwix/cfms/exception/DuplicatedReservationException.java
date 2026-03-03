package io.github.wizwix.cfms.exception;

public class DuplicatedReservationException extends RuntimeException {
  public DuplicatedReservationException() {
    this("Duplicated reservation");
  }

  public DuplicatedReservationException(String message) {
    super(message);
  }
}
