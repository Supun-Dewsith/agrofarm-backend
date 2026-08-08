package com.agrofarm.api.service;

import com.agrofarm.api.dto.FieldRequest;
import com.agrofarm.api.dto.FieldResponse;

import java.util.List;

public interface FieldService {

    FieldResponse createField(FieldRequest request);

    List<FieldResponse> getAllFields();

    FieldResponse getFieldById(Long id);

    List<FieldResponse> getFieldsByFarm(Long farmId);

    FieldResponse updateField(Long id, FieldRequest request);

    void deleteField(Long id);
}
