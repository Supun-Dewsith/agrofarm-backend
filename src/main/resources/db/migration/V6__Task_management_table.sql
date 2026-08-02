CREATE TABLE tasks (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(1000),
                       priority VARCHAR(20) NOT NULL,
                       status VARCHAR(30) NOT NULL,
                       assignee_id BIGINT NOT NULL,
                       due_date DATE NOT NULL,
                       location_tag VARCHAR(255)

);