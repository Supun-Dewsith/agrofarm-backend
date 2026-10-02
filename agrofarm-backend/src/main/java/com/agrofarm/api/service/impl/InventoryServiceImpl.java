package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.inventory.AlertLimitData;
import com.agrofarm.api.dto.inventory.InventoryRequest;
import com.agrofarm.api.dto.inventory.newInventoryRequest;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.InventoryRepository;
import com.agrofarm.api.repository.WeatherRepository;
import com.agrofarm.api.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;
    WeatherRepository weatherRepository;
    CropRepository cropRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, WeatherRepository weatherRepository, CropRepository cropRepository){

        this.inventoryRepository = inventoryRepository;
        this.weatherRepository = weatherRepository;
        this.cropRepository = cropRepository;
    }




    public void addNewInventoryItem(newInventoryRequest inventoryRequest){

        inventoryRepository.insertData(inventoryRequest);

    }




    /* get inventory items */

    public List<InventoryRequest> getInventoryItems(String username) {

        List<FarmIDs> farmIDs =
                getFarmDetails(username);

        List<InventoryRequest> inventoryItems =
                new ArrayList<>();

        for (FarmIDs farm : farmIDs) {

            Long farmID =
                    farm.getFarmIDs();

            List<InventoryRequest> farmInventory =
                    inventoryRepository.getInventoryItems(farmID);

            if (farmInventory != null) {
                inventoryItems.addAll(farmInventory);
            }
        }

        return inventoryItems;
    }




    public List<InventoryRequest> getAdminInventoryItems(String username) {

        List<FarmIDs> farmIDs =
                getAdminFarmDetails(username);

        List<InventoryRequest> inventoryItems =
                new ArrayList<>();

        for (FarmIDs farm : farmIDs) {

            Long farmID =
                    farm.getFarmIDs();

            List<InventoryRequest> farmInventory =
                    inventoryRepository.getInventoryItems(farmID);

            if (farmInventory != null) {
                inventoryItems.addAll(farmInventory);
            }
        }

        return inventoryItems;
    }


    /* getFarm details*/


    public List<FarmIDs> getFarmDetails(String username) {
        Long id = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));



        return cropRepository.findFarmIdsFromUserID(id);



    }

    public List<FarmIDs> getAdminFarmDetails(String username){

        Long id = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));



        return cropRepository.findAdminFarmIdsFromUserID(id);
    }





    /*  low stock details */

    public List<AlertLimitData> getLowstockDetails(String currentUsername) {

        List<FarmIDs> farmIDs = getFarmDetails(currentUsername);
        List<AlertLimitData> lowStockItems = new ArrayList<>();

        for (FarmIDs farm : farmIDs) {

            Long farmID = farm.getFarmIDs();

            List<AlertLimitData> farmLowStockItems =
                    inventoryRepository.getAlertLimitData(farmID);

            lowStockItems.addAll(farmLowStockItems);
        }

        return lowStockItems;
    }







    public List<AlertLimitData> getAdminLowStockDetails(String currentUsername){


        List<FarmIDs> farmIDs = getAdminFarmDetails(currentUsername);
        List<AlertLimitData> lowStockItems = new ArrayList<>();

        for (FarmIDs farm : farmIDs) {

            Long farmID = farm.getFarmIDs();

            List<AlertLimitData> farmLowStockItems =
                    inventoryRepository.getAlertLimitData(farmID);

            lowStockItems.addAll(farmLowStockItems);
        }

        return lowStockItems;
    }



    public void updateInventoryItem(InventoryRequest inventoryrequest){

        Integer farm_id = inventoryrequest.getId();

        inventoryRepository.updateItem(farm_id, inventoryrequest);
    }


}
