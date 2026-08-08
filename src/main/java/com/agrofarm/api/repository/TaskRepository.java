package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Task;
import com.agrofarm.api.util.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByStatus(TaskStatus status);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByFieldId(Long fieldId);

    List<Task> findByAssigneeIdAndStatus(
            Long assigneeId,
            TaskStatus status
    );
}