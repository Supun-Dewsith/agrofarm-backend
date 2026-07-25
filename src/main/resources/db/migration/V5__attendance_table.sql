CREATE TABLE IF NOT EXISTS attendance (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          date DATE NOT NULL,
                                          check_in TIME,
                                          check_out TIME,
                                          status VARCHAR(50)
    );