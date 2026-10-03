package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.workers.RequestPartOne;
import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.repository.WeatherRepository;
import com.agrofarm.api.repository.WorkerRepository;
import com.agrofarm.api.repository.impl.WorkerRepositoryImpl;
import com.agrofarm.api.service.WorkerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkerServiceImpl implements WorkerService {


    private final WeatherRepository weatherRepository;
    private final WorkerRepository workerRepository;

    public WorkerServiceImpl(
            WeatherRepository weatherRepository,
            WorkerRepository workerRepository){
        this.weatherRepository = weatherRepository;
        this.workerRepository = workerRepository;

    }






    @Override
    public List<WorkerRequest> SupervisorRequest(String username) {

        Long supervisorId = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FarmIDs> farms =
                workerRepository.findSupervisorFarmIdsFromUserID(supervisorId);

        List<WorkerRequest> workers = new ArrayList<>();

        if (farms == null || farms.isEmpty()) {
            return workers;
        }

        for (FarmIDs farm : farms) {

            Long farmId = farm.getFarmIDs();
            String farmName = farm.getFarmNames();

            List<Long> workerIds =
                    workerRepository.findWorkerIdByFarmId(farmId);

            if (workerIds == null || workerIds.isEmpty()) {
                continue;
            }

            for (Long workerId : workerIds) {

                RequestPartOne details =
                        workerRepository.findnamePhoneEmailFarmJoindateImage(workerId);

                if (details == null) {
                    continue;
                }

                List<String> tasks =
                        workerRepository.findTasksFromUserID(workerId);

                String status =
                        workerRepository.findStatusFromUserId(workerId);

                WorkerRequest worker = new WorkerRequest();

                worker.setId(workerId);
                worker.setName(details.getName());
                worker.setPhone(details.getPhone());
                worker.setEmail(details.getEmail());

                // Use the farm we are currently looping through
                worker.setFarm(farmName);

                worker.setTasks(tasks != null ? tasks.toArray(new String[0]) : new String[0]);
                worker.setStatus(status);
                worker.setJoinDate(details.getJoinDate());
                worker.setProfilePicture(details.getProfile_picture());
                worker.setRole(details.getRole());

                workers.add(worker);
            }
        }

        return workers;
    }

    @Override
    public List<WorkerRequest> AdminRequest(String username) {

        Long ownerId = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FarmIDs> farms =
                workerRepository.findAdminFarmIdsFromUserID(ownerId);

        List<WorkerRequest> workers = new ArrayList<>();

        if (farms == null || farms.isEmpty()) {
            return workers;
        }

        for (FarmIDs farm : farms) {

            Long farmId = farm.getFarmIDs();
            String farmName = farm.getFarmNames();

            List<Long> workerIds =
                    workerRepository.findWorkerIdByFarmId(farmId);

            if (workerIds == null || workerIds.isEmpty()) {
                continue;
            }

            for (Long workerId : workerIds) {

                RequestPartOne details =
                        workerRepository.findnamePhoneEmailFarmJoindateImage(workerId);

                if (details == null) {
                    continue;
                }

                List<String> tasks =
                        workerRepository.findTasksFromUserID(workerId);

                String status =
                        workerRepository.findStatusFromUserId(workerId);

                WorkerRequest worker = new WorkerRequest();

                worker.setId(workerId);
                worker.setName(details.getName());
                worker.setPhone(details.getPhone());
                worker.setEmail(details.getEmail());

                // Use the farm we are currently looping through
                worker.setFarm(farmName);

                worker.setTasks(tasks != null ? tasks.toArray(new String[0]) : new String[0]);
                worker.setStatus(status);
                worker.setJoinDate(details.getJoinDate());
                worker.setProfilePicture(details.getProfile_picture());
                worker.setRole(details.getRole());

                workers.add(worker);
            }
        }

        return workers;
    }



    @Override
    public boolean deleteWorker(Long id){
        return workerRepository.deleteWorker(id)>0;
    }
}
