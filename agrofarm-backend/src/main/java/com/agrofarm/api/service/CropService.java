package com.agrofarm.api.service;

import com.agrofarm.api.dto.crops.*;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.repository.WeatherRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CropService {
    public List<FarmIDs> getFarmDetails(String username);
    public List<Fields> getFieldDetails(Long id);
    public Long insertCropData(NewCropRequest cropRequest);
    public void insertImage(MultipartFile image, Long cropId);
    public List<CropDetails> getCropDetails(Long farmID);
    public void deleteCrop(Long cropId);
    public void updateCrop(Long cropId, NewCropRequest cropDTO);
    public CropUpdateDetails getCropById(Long cropId);
}
