CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               type VARCHAR(40) NOT NULL,
                               title VARCHAR(160) NOT NULL,
                               message VARCHAR(500) NOT NULL,
                               reference_type VARCHAR(40) NULL,
                               reference_id BIGINT NULL,
                               is_read TINYINT(1) NOT NULL DEFAULT 0,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,
 INDEX idx_notifications_user_created (
                                 user_id,
                                 created_at
                                   ),

INDEX idx_notifications_user_unread (
                                 user_id,
                                 is_read
                                 )
);