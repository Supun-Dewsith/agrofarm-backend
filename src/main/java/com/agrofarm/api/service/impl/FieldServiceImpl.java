package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.FieldRequest;
import com.agrofarm.api.dto.FieldResponse;
import com.agrofarm.api.entity.Field;
import com.agrofarm.api.repository.FieldRepository;
import com.agrofarm.api.repository.FarmRepository;
import com.agrofarm.api.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;

    @Override
    public FieldResponse createField(FieldRequest request) {

        // First check that the farm exists
        farmRepository.findById(request.getFarmId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : "
                                        + request.getFarmId()
                        )
                );

        Field field = Field.builder()
                .farmId(request.getFarmId())
                .fieldName(request.getFieldName())
                .fieldSize(request.getFieldSize())
                .sizeUnit(request.getSizeUnit())
                .latitude(request.getLatitude())
                .longtitude(request.getLongtitude())
                .boundary(request.getBoundary())
                .build();

        Field savedField = fieldRepository.save(field);

        return mapToResponse(savedField);
    }

    @Override
    public List<FieldResponse> getAllFields() {

        return fieldRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FieldResponse getFieldById(Long id) {

        Field field = fieldRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Field not found with id : " + id
                        )
                );

        return mapToResponse(field);
    }

    @Override
    public List<FieldResponse> getFieldsByFarm(Long farmId) {

        farmRepository.findById(farmId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : " + farmId
                        )
                );

        return fieldRepository.findByFarmId(farmId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FieldResponse updateField(
            Long id,
            FieldRequest request
    ) {

        Field field = fieldRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Field not found with id : " + id
                        )
                );

        farmRepository.findById(request.getFarmId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : "
                                        + request.getFarmId()
                        )
                );

        field.setFarmId(request.getFarmId());
        field.setFieldName(request.getFieldName());
        field.setFieldSize(request.getFieldSize());
        field.setSizeUnit(request.getSizeUnit());
        field.setLatitude(request.getLatitude());
        field.setLongtitude(request.getLongtitude());
        field.setBoundary(request.getBoundary());

        Field updatedField = fieldRepository.save(field);

        return mapToResponse(updatedField);
    }

    @Override
    public void deleteField(Long id) {

        Field field = fieldRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Field not found with id : " + id
                        )
                );

        fieldRepository.delete(field);
    }

    private FieldResponse mapToResponse(Field field) {

        return FieldResponse.builder()
                .id(field.getId())
                .farmId(field.getFarmId())
                .fieldName(field.getFieldName())
                .fieldSize(field.getFieldSize())
                .sizeUnit(field.getSizeUnit())
                .latitude(field.getLatitude())
                .longtitude(field.getLongtitude())
                .boundary(field.getBoundary())
                .build();
    }
}