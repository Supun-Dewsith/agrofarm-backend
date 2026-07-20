package com.agrofarm.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class GlobalExceptionHandler {
    public ResponseEntity<Map<String, Object>> handleLocationNotFound(LocationNotFoundException ex) {
        Map<String, Object> response = Map.of(
                "error", "LOCATION_MISSING",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
