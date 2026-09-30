package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.inventory.AlertLimitData;
import com.agrofarm.api.dto.inventory.InventoryRequest;
import com.agrofarm.api.dto.inventory.newInventoryRequest;
import com.agrofarm.api.repository.InventoryRepository;
import com.agrofarm.api.service.InventoryService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Repository

public class InventoryRepositoryImpl implements InventoryRepository {
    JdbcTemplate jdbcTemplate;

    public InventoryRepositoryImpl (JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }



    @Override
    public void insertData(newInventoryRequest newInventoryRequest) {

        String sql = """
        INSERT INTO inventory (inventory_name, category, quantity, unit, alertLimit, farm_id)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        String name = newInventoryRequest.getName();
        String category = newInventoryRequest.getCategory();
        Double quantity = newInventoryRequest.getQuantity();
        String unit = newInventoryRequest.getUnit();
        Double alertLimit = newInventoryRequest.getAlertLimit();
        Integer farmid = newInventoryRequest.getFarmIDs();

        jdbcTemplate.update(sql, name, category, quantity, unit, alertLimit, farmid);
    }

    public List<AlertLimitData> getAlertLimitData(Long farmID){

        String sql = "SELECT id, inventory_name, category, quantity, alertLimit, farm_id, unit FROM inventory WHERE quantity <= alertLimit AND farm_id = ?";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                AlertLimitData alertLimitData = new AlertLimitData();
                alertLimitData.setId(rs.getInt("id"));
                alertLimitData.setName(rs.getString("inventory_name"));

                Integer currentquantity = rs.getInt("quantity");
                Integer alertLimit = rs.getInt("alertLimit");
                String unit = rs.getString("unit");

                String currentquantityString = currentquantity.toString();
                String alertLimitString = alertLimit.toString();

                String current = currentquantityString + " " + unit;
                String reorderAt = alertLimitString + " " + unit;


                alertLimitData.setCurrent(current);
                alertLimitData.setReorderAt(reorderAt);
                alertLimitData.setFarmId(rs.getInt("farm_id"));
                alertLimitData.setCategory(rs.getString("category"));
                return alertLimitData;
            }, farmID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user return null; }
            return null;
        }

    }


    @Override
    public List<InventoryRequest> getInventoryItems(Long farmID){

        String sql = "SELECT id, inventory_name, category, quantity, alertLimit, farm_id, unit FROM inventory WHERE farm_id = ?";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                InventoryRequest inventoryRequest = new InventoryRequest();
                inventoryRequest.setId(rs.getInt("id"));
                inventoryRequest.setName(rs.getString("inventory_name"));
                inventoryRequest.setCategory(rs.getString("category"));
                inventoryRequest.setQuantity(rs.getDouble("quantity"));
                inventoryRequest.setUnit(rs.getString("unit"));
                inventoryRequest.setAlertLimit(rs.getDouble("alertLimit"));
                inventoryRequest.setFarmIds(rs.getLong("farm_id"));


                return inventoryRequest;
            }, farmID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user return null; }
            return null;
        }

    }

}
