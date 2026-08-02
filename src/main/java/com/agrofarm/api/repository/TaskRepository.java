package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Task;
import com.agrofarm.api.util.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Count tasks by status
    long countByStatus(TaskStatus status);

    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);

}