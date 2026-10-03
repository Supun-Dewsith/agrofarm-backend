package com.agrofarm.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/farm-location-search")
public class FarmLocationSearchController {

    private final JsonMapper jsonMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<String, CachedResult> cache = new LinkedHashMap<>();
    private long lastRequestTime;

    public FarmLocationSearchController(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public record LocationMatch(
            String label,
            double latitude,
            double longitude
    ) {}

    private record CachedResult(
            long savedAt,
            List<LocationMatch> matches
    ) {}

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public synchronized List<LocationMatch> search(
            @RequestParam("q") String query
    ) {
        String address = query.trim().replaceAll("\\s+", " ");

        if (address.length() < 3 || address.length() > 250) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter an address between 3 and 250 characters."
            );
        }

        String key = address.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();

        CachedResult cached = cache.get(key);

        if (cached != null
                && now - cached.savedAt() < Duration.ofHours(6).toMillis()) {
            return cached.matches();
        }

        long requestTime = System.nanoTime();

        if (lastRequestTime != 0
                && requestTime - lastRequestTime < 1_100_000_000L) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait a moment before searching again."
            );
        }

        lastRequestTime = requestTime;

        String url = "https://nominatim.openstreetmap.org/search"
                + "?format=jsonv2"
                + "&countrycodes=lk"
                + "&limit=5"
                + "&q=" + URLEncoder.encode(
                address,
                StandardCharsets.UTF_8
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "AgroFarm-FarmManagement/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 429) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Address search is busy. Please try again later."
                );
            }

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "The address provider could not complete the search."
                );
            }

            var results = jsonMapper.readTree(response.body());

            if (!results.isArray()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid response from the address provider."
                );
            }

            List<LocationMatch> matches = new ArrayList<>();

            for (var result : results) {
                if (!result.hasNonNull("display_name")
                        || !result.hasNonNull("lat")
                        || !result.hasNonNull("lon")) {
                    continue;
                }

                double latitude = Double.parseDouble(
                        result.get("lat").asText()
                );

                double longitude = Double.parseDouble(
                        result.get("lon").asText()
                );

                if (!Double.isFinite(latitude)
                        || !Double.isFinite(longitude)
                        || Math.abs(latitude) > 90
                        || Math.abs(longitude) > 180) {
                    continue;
                }

                matches.add(new LocationMatch(
                        result.get("display_name").asText(),
                        latitude,
                        longitude
                ));
            }

            List<LocationMatch> saved = List.copyOf(matches);

            cache.put(key, new CachedResult(now, saved));

            while (cache.size() > 256) {
                cache.remove(cache.keySet().iterator().next());
            }

            return saved;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Address search was interrupted.",
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Cannot connect to the address provider.",
                    exception
            );
        }
    }
}
