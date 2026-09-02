package com.agrofarm.api.repository;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRepository {
    public List<FarmIDs> findFarmIdsFromUserID(Long ID);
    public List<Fields> findFieldsFromFarmID(Long ID);
}
