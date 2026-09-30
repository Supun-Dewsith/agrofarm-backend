package com.agrofarm.api.controller;

import com.agrofarm.api.dto.HarvestRequest;
import com.agrofarm.api.dto.MarketPriceRequest;
import com.agrofarm.api.service.impl.HarvestServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/harvest")
@PreAuthorize(
        "hasAnyAuthority('ADMIN','SUPERVISOR','FARMER','VIEW_HARVEST')"
)
public class HarvestController {

    private final HarvestServiceImpl harvestService;

    public HarvestController(HarvestServiceImpl harvestService) {
        this.harvestService = harvestService;
    }

    @GetMapping("/options")
    public ResponseEntity<List<Map<String, Object>>> options() {
        return ResponseEntity.ok(
                harvestService.getCropOptions(username())
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(
                harvestService.getSummary(username())
        );
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> records() {
        return ResponseEntity.ok(
                harvestService.getRecords(username())
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody HarvestRequest request
    ) {
        return ResponseEntity.ok(
                harvestService.createRecord(username(), request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable("id") long id,
            @RequestBody HarvestRequest request
    ) {
        return ResponseEntity.ok(
                harvestService.updateRecord(username(), id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        harvestService.deleteRecord(username(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/market-prices")
    public ResponseEntity<List<Map<String, Object>>> marketPrices(
            @RequestParam("cropId") long cropId
    ) {
        return ResponseEntity.ok(
                harvestService.getMarketRecommendations(username(), cropId)
        );
    }

    @PostMapping("/market-prices")
    public ResponseEntity<Map<String, Object>> addMarketPrice(
            @RequestBody MarketPriceRequest request
    ) {
        return ResponseEntity.ok(
                harvestService.addMarketPrice(username(), role(), request)
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleError(
            ResponseStatusException exception
    ) {
        String message = exception.getReason() == null
                ? "Request failed."
                : exception.getReason();

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(Map.of("message", message));
    }

    private String username() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication == null ? "" : authentication.getName();
    }

    private String role() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "";
        }

        return authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .filter(value ->
                        value.equals("ADMIN") || value.equals("SUPERVISOR")
                )
                .findFirst()
                .orElse("");
    }
}