package com.agrofarm.api.controller;


import com.agrofarm.api.dto.inventory.AlertLimitData;
import com.agrofarm.api.dto.inventory.InventoryRequest;
import com.agrofarm.api.dto.inventory.newInventoryRequest;
import com.agrofarm.api.service.CropService;
import com.agrofarm.api.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController

@RequestMapping("/api/inventory")
public class InventoryController {

    InventoryService inventoryService;
    CropService cropService;

    public InventoryController(InventoryService inventoryService, CropService cropService){

        this.inventoryService = inventoryService;
        this.cropService = cropService;
    }


    @PostMapping("/new")
    public ResponseEntity<?> addNewInventoryItem(@RequestBody newInventoryRequest inventoryrequest){

        inventoryService.addNewInventoryItem(inventoryrequest);
        return ResponseEntity.ok().build();

    }


    @GetMapping("/lowstock")
    public List<AlertLimitData> getLowStockInventory(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);

        if (role.equals("FARMER")|| role.equals("SUPERVISOR")) {

            return inventoryService.getLowstockDetails(currentUsername);
        }
        else if (role.equals("ADMIN")) {
            return inventoryService.getAdminLowStockDetails(currentUsername);
        }
        else {
            return Collections.emptyList();
        }
    }


    @GetMapping("/all")
    public List<InventoryRequest> getAllInventoryItems(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);


        if (role.equals("FARMER")|| role.equals("SUPERVISOR")) {

            return inventoryService.getInventoryItems(currentUsername);
        }
        else if (role.equals("ADMIN")) {
            return inventoryService.getAdminInventoryItems(currentUsername);
        }
        else {
            return Collections.emptyList();
        }

    }

}
