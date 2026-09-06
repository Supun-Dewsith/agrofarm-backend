CREATE TABLE task_assignments (

                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                  task_id BIGINT NOT NULL,

                                  worker_id BIGINT NOT NULL,


                                  CONSTRAINT fk_assignment_task
                                      FOREIGN KEY (task_id)
                                          REFERENCES tasks(id)
                                          ON DELETE CASCADE,


                                  CONSTRAINT fk_assignment_worker
                                      FOREIGN KEY (worker_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE

);