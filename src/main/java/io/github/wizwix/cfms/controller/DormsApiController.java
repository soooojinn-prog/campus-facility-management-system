package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.exception.NotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dorms")
@RequiredArgsConstructor
public class DormsApiController {
  @GetMapping
  public ResponseEntity<?> getDorms() {
    throw new NotImplementedException();
  }
}
