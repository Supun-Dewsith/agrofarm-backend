package com.agrofarm.api.service;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import com.agrofarm.api.dto.crops.NewCropRequest;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.repository.WeatherRepository;

import java.util.List;

public interface CropService {
    public List<FarmIDs> getFarmDetails(String username);
    public List<Fields> getFieldDetails(Long id);
    void addNewCrop(NewCropRequest request);
}
