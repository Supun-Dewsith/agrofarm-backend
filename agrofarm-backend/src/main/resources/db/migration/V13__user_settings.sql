ALTER TABLE users
    ADD COLUMN first_name VARCHAR(100) NULL,
    ADD COLUMN last_name VARCHAR(100) NULL;

CREATE TABLE user_settings (
                               user_id BIGINT PRIMARY KEY,

                               disease_alerts TINYINT(1) NOT NULL DEFAULT 1,
                               low_stock_warnings TINYINT(1) NOT NULL DEFAULT 1,
                               task_reminders TINYINT(1) NOT NULL DEFAULT 1,
                               harvest_updates TINYINT(1) NOT NULL DEFAULT 1,
                               weather_alerts TINYINT(1) NOT NULL DEFAULT 1,

                               two_factor_authentication TINYINT(1) NOT NULL DEFAULT 0,

                               farm_name VARCHAR(200) NOT NULL DEFAULT 'AgriSmart Farm',
                               timezone VARCHAR(100) NOT NULL DEFAULT 'UTC-7 (Pacific Time)',
                               currency VARCHAR(20) NOT NULL DEFAULT 'USD ($)',
                               auto_backup TINYINT(1) NOT NULL DEFAULT 1,
                               ai_disease_detection TINYINT(1) NOT NULL DEFAULT 1,
                               weather_sync TINYINT(1) NOT NULL DEFAULT 1,

                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                               CONSTRAINT fk_user_settings_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO user_settings (user_id)
SELECT id FROM users;
