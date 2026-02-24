package io.github.wizwix.cfms.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonErrorResponseWriter {
  private final ObjectMapper objectMapper;

  public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
    ResponseError body = new ResponseError(status.value(), status.getReasonPhrase(), message, System.currentTimeMillis());
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
