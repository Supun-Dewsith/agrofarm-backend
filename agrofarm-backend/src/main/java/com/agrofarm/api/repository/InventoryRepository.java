package com.agrofarm.api.repository;

import com.agrofarm.api.dto.inventory.AlertLimitData;
import com.agrofarm.api.dto.inventory.InventoryRequest;
import com.agrofarm.api.dto.inventory.newInventoryRequest;

import java.util.List;

public interface InventoryRepository {

    public void insertData(newInventoryRequest newInventoryRequest);

    public List<AlertLimitData> getAlertLimitData(Long farmID);

    public List<InventoryRequest> getInventoryItems(Long farmID);
}
