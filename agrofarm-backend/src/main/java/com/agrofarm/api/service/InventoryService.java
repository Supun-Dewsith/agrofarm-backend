package com.agrofarm.api.service;

import com.agrofarm.api.dto.inventory.AlertLimitData;
import com.agrofarm.api.dto.inventory.InventoryRequest;
import com.agrofarm.api.dto.inventory.newInventoryRequest;

import java.util.List;

public interface InventoryService {

    public void addNewInventoryItem(newInventoryRequest inventoryRequest);
    public List<InventoryRequest> getInventoryItems(String username);

    public List<AlertLimitData> getLowstockDetails(String currentUsername);

    public List<AlertLimitData> getAdminLowStockDetails(String currentUsername);

    public List<InventoryRequest> getAdminInventoryItems(String username);

    public void updateInventoryItem(InventoryRequest inventoryrequest);
}
