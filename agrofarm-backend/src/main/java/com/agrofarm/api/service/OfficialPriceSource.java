package com.agrofarm.api.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OfficialPriceSource {

    private static final String INDEX =
            "https://www.cbsl.gov.lk/en/statistics/economic-indicators/price-report";

    private static final String PDF_BASE =
            "https://www.cbsl.gov.lk/sites/default/files/cbslweb_documents/statistics/pricerpt/price_report_%s_e.pdf";

    private static final String USER_AGENT =
            "AgroFarm/1.0 (official-price-sync)";

    private static final int INDEX_MAX_BYTES = 2_000_000;
    private static final int PDF_MAX_BYTES = 8_000_000;

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Colombo");

    private static final DateTimeFormatter REPORT_DATE_FORMAT =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d MMMM uuuu")
                    .toFormatter(Locale.ENGLISH);

    private static final Pattern FILE = Pattern.compile(
            "(?i)^https?://[^/]+/.*?/price_report_(\\d{8})_e\\.pdf$"
    );

    private static final Pattern REPORT_TITLE = Pattern.compile(
            "(?i)Daily\\s+Price\\s+Report\\s*[-–—:]\\s*" +
                    "(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})"
    );

    private static final Pattern ROW = Pattern.compile(
            "^\\s*(.+?)\\s+Rs\\./kg\\s+(.+?)\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CELL = Pattern.compile(
            "(?i)(?:\\d{1,3}(?:,\\d{3})+|\\d+)\\.\\d{2}|n\\.a\\."
    );

    private static final Map<String, String> CROP_NAMES = cropNames();

    public record Price(
            String commodity,
            String market,
            BigDecimal priceLkrPerKg
    ) {
    }

    public record Snapshot(
            LocalDate reportDate,
            Instant fetchedAt,
            String sourceUrl,
            List<Price> prices
    ) {
    }

    private record ReportLink(
            LocalDate date,
            String detailUrl,
            String pdfUrl
    ) {
    }

    public Snapshot download() throws IOException {
        Document index = getHtml(INDEX, INDEX_MAX_BYTES);

        IOException failure = null;

        for (ReportLink link : findReports(index).stream().limit(3).toList()) {
            try {
                return downloadReport(link);
            } catch (IOException exception) {
                failure = exception;
            }
        }

        throw new IOException(
                "No readable recent CBSL report: " +
                        (failure == null ? "no links" : failure.getMessage()),
                failure
        );
    }

    private Snapshot downloadReport(ReportLink latest) throws IOException {
        String pdfUrl = latest.pdfUrl();

        if (pdfUrl == null) {
            Document detail = getHtml(
                    latest.detailUrl(),
                    INDEX_MAX_BYTES
            );

            pdfUrl = findPdfUrl(detail, latest.date());
        }

        if (pdfUrl == null) {
            pdfUrl = PDF_BASE.formatted(
                    latest.date().format(DateTimeFormatter.BASIC_ISO_DATE)
            );
        }

        byte[] bytes = downloadPdf(pdfUrl);

        try (var pdf = Loader.loadPDF(bytes)) {

            if (pdf.getNumberOfPages() < 2) {
                throw new IOException("Report layout changed");
            }

            var stripper = new PDFTextStripper();

            stripper.setSortByPosition(true);
            stripper.setStartPage(2);
            stripper.setEndPage(2);

            String text = stripper
                    .getText(pdf)
                    .replace('\u00a0', ' ');

            String compact = text.replaceAll("\\s+", " ");

            String reportDate = latest.date().format(
                    DateTimeFormatter.ofPattern(
                            "d MMMM uuuu",
                            Locale.ENGLISH
                    )
            );

            if (!compact.contains(reportDate)
                    || !compact.contains("Wholesale")
                    || !compact.contains("Retail")
                    || !compact.contains("Pettah")
                    || !compact.contains("Dambulla")) {

                throw new IOException("Unexpected report headings");
            }

            List<Price> prices = parseRows(text);

            if (prices.isEmpty()) {
                throw new IOException(
                        "No supported wholesale prices found"
                );
            }

            return new Snapshot(
                    latest.date(),
                    Instant.now(),
                    pdfUrl,
                    List.copyOf(prices)
            );
        }
    }

    private static Document getHtml(
            String url,
            int maxBytes
    ) throws IOException {

        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(20_000)
                .maxBodySize(maxBytes)
                .followRedirects(true)
                .get();
    }

    private static byte[] downloadPdf(String url) throws IOException {
        var response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(30_000)
                .maxBodySize(PDF_MAX_BYTES)
                .ignoreContentType(true)
                .followRedirects(true)
                .execute();

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Official report returned HTTP " +
                            response.statusCode()
            );
        }

        byte[] bytes = response.bodyAsBytes();

        if (bytes.length == 0
                || bytes.length >= PDF_MAX_BYTES) {

            throw new IOException(
                    "Report is empty or too large"
            );
        }

        return bytes;
    }

    private static List<ReportLink> findReports(
            Document index
    ) throws IOException {

        var candidates =
                new TreeMap<LocalDate, ReportLink>();

        for (Element link : index.select("a[href]")) {

            String url = cleanUrl(
                    link.absUrl("href")
            );

            if (!isOfficialUrl(url)) {
                continue;
            }

            LocalDate date = reportDate(
                    url,
                    link.text()
            );

            if (date == null
                    || date.isAfter(LocalDate.now(ZONE))) {
                continue;
            }

            String pdfUrl =
                    isReportPdf(url, date)
                            ? url
                            : null;

            ReportLink candidate =
                    new ReportLink(date, url, pdfUrl);

            ReportLink existing =
                    candidates.get(date);

            if (existing == null
                    || (existing.pdfUrl() == null
                    && pdfUrl != null)) {

                candidates.put(date, candidate);
            }
        }

        if (candidates.isEmpty()) {
            throw new IOException(
                    "No official report links found"
            );
        }

        return List.copyOf(
                candidates.descendingMap().values()
        );
    }

    private static String findPdfUrl(
            Document detail,
            LocalDate expectedDate
    ) {

        for (Element element : detail.select(
                "a[href], iframe[src], embed[src], object[data]"
        )) {

            String attribute;

            if (element.hasAttr("href")) {
                attribute = "href";
            } else if (element.hasAttr("src")) {
                attribute = "src";
            } else {
                attribute = "data";
            }

            String url = cleanUrl(
                    element.absUrl(attribute)
            );

            if (!isOfficialUrl(url)) {
                continue;
            }

            if (isReportPdf(url, expectedDate)) {
                return url;
            }
        }

        return null;
    }

    private static boolean isReportPdf(
            String url,
            LocalDate expectedDate
    ) {

        Matcher matcher =
                FILE.matcher(cleanUrl(url));

        if (!matcher.matches()) {
            return false;
        }

        return LocalDate.parse(
                matcher.group(1),
                DateTimeFormatter.BASIC_ISO_DATE
        ).equals(expectedDate);
    }

    private static LocalDate reportDate(
            String url,
            String label
    ) {

        Matcher file =
                FILE.matcher(cleanUrl(url));

        if (file.matches()) {
            try {
                return LocalDate.parse(
                        file.group(1),
                        DateTimeFormatter.BASIC_ISO_DATE
                );
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        String cleanLabel = label == null
                ? ""
                : label.replaceAll("\\s+", " ").trim();

        Matcher title =
                REPORT_TITLE.matcher(cleanLabel);

        if (!title.find()) {
            return null;
        }

        try {
            return LocalDate.parse(
                    title.group(1),
                    REPORT_DATE_FORMAT
            );
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static boolean isOfficialUrl(String url) {
        try {
            var uri = java.net.URI.create(url);

            return "https".equalsIgnoreCase(uri.getScheme())
                    && "www.cbsl.gov.lk".equalsIgnoreCase(uri.getHost());

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static String cleanUrl(String url) {
        if (url == null) {
            return "";
        }

        int query = url.indexOf('?');
        int fragment = url.indexOf('#');
        int end = url.length();

        if (query >= 0) {
            end = Math.min(end, query);
        }

        if (fragment >= 0) {
            end = Math.min(end, fragment);
        }

        return url.substring(0, end);
    }

    static List<Price> parseRows(
            String text
    ) throws IOException {

        String headings =
                text.replaceAll("\\s+", " ");

        if (!headings.contains(
                "Wholesale Prices Retail Prices"
        )) {
            throw new IOException(
                    "Wholesale/Retail headings changed"
            );
        }

        if (!headings.contains(
                "Pettah Dambulla Pettah Dambulla Narahenpita"
        )) {
            throw new IOException(
                    "Market column order changed"
            );
        }

        if (!headings.contains(
                "Yesterday Today Yesterday Today Yesterday Today " +
                        "Yesterday Today Yesterday Today"
        )) {
            throw new IOException(
                    "Price column order changed"
            );
        }

        List<Price> prices = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String line : text.split("\\R")) {

            Matcher row =
                    ROW.matcher(line.strip());

            if (!row.matches()) {
                continue;
            }

            String rawName = row.group(1)
                    .replaceAll("\\s+", " ")
                    .trim();

            String name =
                    CROP_NAMES.get(
                            rawName.toLowerCase(Locale.ROOT)
                    );

            if (name == null) {
                continue;
            }

            String[] cells =
                    row.group(2)
                            .strip()
                            .split("\\s+");

            if (cells.length != 10
                    || !seen.add(name)) {

                throw new IOException(
                        "Unexpected price row"
                );
            }

            for (String cell : cells) {
                if (!CELL.matcher(cell).matches()) {
                    throw new IOException(
                            "Unexpected price cell"
                    );
                }
            }

            add(
                    prices,
                    name,
                    "Pettah",
                    cells[1]
            );

            add(
                    prices,
                    name,
                    "Dambulla",
                    cells[3]
            );
        }

        if (seen.size() != CROP_NAMES.size()) {
            throw new IOException(
                    "Incomplete supported commodity table: " +
                            seen.size()
            );
        }

        return prices;
    }

    private static void add(
            List<Price> prices,
            String crop,
            String market,
            String value
    ) throws IOException {

        if (value.equalsIgnoreCase("n.a.")) {
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(
                    value.replace(",", "")
            );
        } catch (NumberFormatException exception) {
            throw new IOException(
                    "Invalid price",
                    exception
            );
        }

        if (price.signum() <= 0) {
            throw new IOException(
                    "Invalid price"
            );
        }

        prices.add(
                new Price(
                        crop,
                        market,
                        price
                )
        );
    }

    private static Map<String, String> cropNames() {
        Map<String, String> names =
                new HashMap<>();

        for (String name : List.of(
                "Beans",
                "Carrot",
                "Cabbage",
                "Tomato",
                "Brinjal",
                "Pumpkin",
                "Snake gourd",
                "Green Chilli",
                "Lime",
                "Red Onion (Local)",
                "Big Onion (Local)",
                "Potato (Local)",
                "Banana (Sour)",
                "Papaw",
                "Pineapple"
        )) {
            names.put(
                    name.toLowerCase(Locale.ROOT),
                    name
            );
        }

        return Map.copyOf(names);
    }
}