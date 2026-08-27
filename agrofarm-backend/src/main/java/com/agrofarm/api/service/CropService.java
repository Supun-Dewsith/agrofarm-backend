package com.agrofarm.api.service;

import com.agrofarm.api.dto.FarmIDs;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.repository.WeatherRepository;

import java.util.List;

public interface CropService {
    public List<FarmIDs> getFarmDetails(String username);
}
