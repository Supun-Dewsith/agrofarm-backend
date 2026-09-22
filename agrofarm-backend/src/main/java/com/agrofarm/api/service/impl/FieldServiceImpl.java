package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.FieldRequest;
import com.agrofarm.api.dto.FieldResponse;
import com.agrofarm.api.entity.Field;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.FarmRepository;
import com.agrofarm.api.repository.FieldRepository;
import com.agrofarm.api.service.FarmAccessService;
import com.agrofarm.api.service.FieldService;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;
    private final FarmAccessService farmAccessService;

    @Override
    public FieldResponse createField(FieldRequest request) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireManageFields(
                user,
                request.getFarmId()
        );

        requireExistingFarm(request.getFarmId());

        Field field = new Field();

        applyRequest(field, request);

        Field savedField = fieldRepository.save(field);

        return mapToResponse(savedField);
    }

    @Override
    public List<FieldResponse> getAllFields() {

        UserEntity user = farmAccessService.currentUser();

        List<Field> fields;

        if (user.getRole() == Role.ADMIN) {
            fields = fieldRepository.findAll();
        } else {
            fields = fieldRepository.findAssignedToUser(user.getId());
        }

        return fields.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FieldResponse getFieldById(Long id) {

        UserEntity user = farmAccessService.currentUser();

        Field field = findField(id);

        farmAccessService.requireRead(
                user,
                field.getFarmId()
        );

        return mapToResponse(field);
    }

    @Override
    public List<FieldResponse> getFieldsByFarm(Long farmId) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireRead(user, farmId);

        requireExistingFarm(farmId);

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

        UserEntity user = farmAccessService.currentUser();

        Field field = findField(id);

        // Check access to the field's existing farm.
        farmAccessService.requireManageFields(
                user,
                field.getFarmId()
        );

        // Also check the destination farm if the farm is changed.
        farmAccessService.requireManageFields(
                user,
                request.getFarmId()
        );

        requireExistingFarm(request.getFarmId());

        applyRequest(field, request);

        Field updatedField = fieldRepository.save(field);

        return mapToResponse(updatedField);
    }

    @Override
    public void deleteField(Long id) {

        UserEntity user = farmAccessService.currentUser();

        Field field = findField(id);

        farmAccessService.requireManageFields(
                user,
                field.getFarmId()
        );

        fieldRepository.delete(field);
    }

    private Field findField(Long id) {

        return fieldRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Field not found"
                        )
                );
    }

    private void requireExistingFarm(Long farmId) {

        farmRepository.findById(farmId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Farm not found"
                        )
                );
    }

    private void applyRequest(
            Field field,
            FieldRequest request
    ) {

        field.setFarmId(request.getFarmId());
        field.setFieldName(request.getFieldName());
        field.setFieldSize(request.getFieldSize());
        field.setSizeUnit(request.getSizeUnit());
        field.setLatitude(request.getLatitude());
        field.setLongtitude(request.getLongtitude());
        field.setBoundary(request.getBoundary());
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