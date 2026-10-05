package com.agrofarm.api.service;

import com.agrofarm.api.dto.workers.WorkerRequest;

import java.util.List;

public interface WorkerService {

    public List<WorkerRequest> AdminRequest(String username);
    public List<WorkerRequest> SupervisorRequest(String username);
    public boolean deleteWorker(Long id);
}
