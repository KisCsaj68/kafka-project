package com.kafka.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/healthz")
    public ResponseEntity<String> getHealthz() {
        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
