package io.github.wizwix.cfms.global.error;

import io.github.wizwix.cfms.exception.NotImplementedException;
import io.github.wizwix.cfms.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
  private final JsonErrorResponseWriter writer;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseError> handleGeneral(Exception e, HttpServletResponse response) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private ResponseEntity<ResponseError> buildResponse(HttpStatus httpStatus, String message) {
    ResponseError body = new ResponseError(
        httpStatus.value(),
        httpStatus.getReasonPhrase(),
        message,
        System.currentTimeMillis()
    );
    return ResponseEntity.status(httpStatus).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ResponseError> handleIllegalArgument(IllegalArgumentException e, HttpServletResponse response) {
    return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  /// IllegalStateException → 409 Conflict
  /// 사용 사례:
  ///   - 시설 예약 시간 충돌 ("선택한 시간에 이미 예약이 존재합니다")
  ///   - PENDING이 아닌 예약 취소 시도 ("승인 대기 중인 예약만 취소할 수 있습니다")
  ///   - 본인 예약이 아닌 경우 ("본인의 예약만 취소할 수 있습니다")
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ResponseError> handleIllegalState(IllegalStateException e, HttpServletResponse response) {
    return buildResponse(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(NotImplementedException.class)
  public ResponseEntity<ResponseError> handleNotImplemented(NotImplementedException e, HttpServletResponse response) {
    return buildResponse(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ResponseError> handleTooManyRequests(TooManyRequestsException e, HttpServletResponse response) {
    return buildResponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseError> handleValidation(MethodArgumentNotValidException e, HttpServletResponse response) {
    String detail = e.getBindingResult().getFieldErrors().stream().map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.joining(", "));
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + detail);
  }
}
