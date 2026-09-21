package com.agrofarm.api.repository;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.workers.RequestPartOne;
import com.agrofarm.api.dto.workers.WorkerRequest;

import java.util.List;

public interface WorkerRepository {

    public List<FarmIDs> findAdminFarmIdsFromUserID(Long id);
    public List<Long>findWorkerIdByFarmId(Long farmId);
    public RequestPartOne findnamePhoneEmailFarmJoindateImage(Long id);
    public List<String> findTasksFromUserID(Long id);
    public String findStatusFromUserId(Long id);
    public Long findFarmIdFromUserID(Long id);
    public List<FarmIDs> findSupervisorFarmIdsFromUserID(Long id);
}