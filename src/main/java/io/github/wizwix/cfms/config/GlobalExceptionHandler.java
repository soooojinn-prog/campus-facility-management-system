package io.github.wizwix.cfms.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
    Map<String, String> body = new HashMap<>();
    body.put("message", "Internal Server Error");
    return ResponseEntity.internalServerError().body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
    Map<String, String> body = new HashMap<>();
    body.put("message", e.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> body = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(err -> body.put(err.getField(), err.getDefaultMessage()));
    return ResponseEntity.badRequest().body(body);
  }
}
