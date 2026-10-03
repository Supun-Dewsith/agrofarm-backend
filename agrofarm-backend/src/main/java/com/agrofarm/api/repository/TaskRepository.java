package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Task;
import com.agrofarm.api.util.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    List<Task> findAll();

    Optional<Task> findById(Long id);

    Task update(Long id, Task task);

    void delete(Long id);

    void deleteAssignments(Long taskId);

    void addWorker(Long taskId, Long workerId);

    List<Long> findWorkerIds(Long taskId);

    Long findFarmIdByName(String farmName);

    Long findFieldIdByName(String fieldName, Long farmId);

    Long findUserIdByName(String userName);

    List<Long> findUserIdsByNames(List<String> names);

    String findFarmNameById(Long farmId);

    String findFieldNameById(Long fieldId);

    String findUserNameById(Long userId);

    List<String> findWorkerNames(Long taskId);

    void updateStatus(Long id, TaskStatus status);
}