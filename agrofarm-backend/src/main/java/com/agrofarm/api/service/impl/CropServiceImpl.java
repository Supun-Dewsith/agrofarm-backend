package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import com.agrofarm.api.dto.crops.NewCropRequest;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.repository.WeatherRepository;
import com.agrofarm.api.service.CropService;
import com.agrofarm.api.service.WeatherService;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class CropServiceImpl implements CropService {

    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;
    private CropRepository cropRepository;


    public CropServiceImpl(WeatherRepository weatherRepository, UserRepository userRepository, CropRepository cropRepository){
        this.userRepository = userRepository;
        this.weatherRepository = weatherRepository;
        this.cropRepository = cropRepository;
    }

    @Override
    public List<FarmIDs> getFarmDetails(String username) {
        Long id = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cropRepository.findFarmIdsFromUserID(id);

        
    }

    @Override
    public List<Fields> getFieldDetails(Long id){

        return cropRepository.findFieldsFromFarmID(id);
    }



    @Override
    public void addNewCrop(NewCropRequest request){
        cropRepository.addNewCrop(request);
    }


}
