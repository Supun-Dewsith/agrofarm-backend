package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.repository.WeatherRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkerServiceImpl {

    WeatherRepository weatherRepository;

    public WorkerServiceImpl(WeatherRepository weatherRepository){
        this.weatherRepository = weatherRepository;
    }

    public WorkerRequest AdminRequest (String username){

        Long userId = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return
    }
}
