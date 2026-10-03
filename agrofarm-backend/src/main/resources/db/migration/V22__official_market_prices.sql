CREATE TABLE official_price_sync (
                                     id INT PRIMARY KEY,
                                     last_checked_ms BIGINT NULL,
                                     last_success_ms BIGINT NULL,
                                     next_check_ms BIGINT NOT NULL DEFAULT 0,
                                     last_error VARCHAR(300) NULL
);

INSERT INTO official_price_sync (id) VALUES (1);

CREATE TABLE official_price_reports (
                                        report_date DATE PRIMARY KEY,
                                        fetched_ms BIGINT NOT NULL,
                                        source_url VARCHAR(500) NOT NULL
);

CREATE TABLE official_price_values (
                                       report_date DATE NOT NULL,
                                       commodity VARCHAR(100) NOT NULL,
                                       market VARCHAR(100) NOT NULL,
                                       price_lkr_per_kg DECIMAL(12,2) NOT NULL,

                                       PRIMARY KEY (report_date, commodity, market),

                                       CONSTRAINT fk_official_price_report
                                           FOREIGN KEY (report_date)
                                               REFERENCES official_price_reports(report_date),

                                       CONSTRAINT chk_official_price_positive
                                           CHECK (price_lkr_per_kg > 0)
);