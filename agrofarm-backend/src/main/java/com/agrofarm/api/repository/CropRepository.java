package com.agrofarm.api.repository;

import com.agrofarm.api.dto.FarmIDs;

import java.util.List;

public interface CropRepository {
    public List<FarmIDs> findFarmIdsFromUserID(Long ID);
}
