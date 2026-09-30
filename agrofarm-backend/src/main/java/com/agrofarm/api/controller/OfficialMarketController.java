package com.agrofarm.api.controller;

import com.agrofarm.api.service.OfficialPriceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/harvest/official-prices")
@PreAuthorize(
        "hasAnyAuthority('ADMIN','SUPERVISOR','FARMER','VIEW_HARVEST')"
)
public class OfficialMarketController {

    private final OfficialPriceService service;

    public OfficialMarketController(OfficialPriceService service) {
        this.service = service;
    }

    @GetMapping
    public OfficialPriceService.Result latest() {
        return service.latest();
    }
}

