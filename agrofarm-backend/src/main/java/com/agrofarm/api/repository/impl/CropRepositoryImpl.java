package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import com.agrofarm.api.repository.CropRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class CropRepositoryImpl implements CropRepository {
    private final JdbcTemplate template;

    public CropRepositoryImpl(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<FarmIDs> findFarmIdsFromUserID(Long ID) {


        String sql = "SELECT id, farm_name FROM farms WHERE owner_id = ?";
        try {
            return template.query(sql, (rs, rowNum) -> {

                FarmIDs IDs = new FarmIDs();


                IDs.setFarmIDs(rs.getLong("id"));
                IDs.setFarmNames(rs.getString("farm_name"));
                return IDs;
            }, ID);

        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }
    }

    @Override
    public List<Fields> findFieldsFromFarmID(Long ID) {

        String sql = "SELECT id, field_name, field_size, size_unit WHERE farm_id = ?";
        try {
            return template.query(sql, (rs, rowNum) -> {

                Fields fields = new Fields();

                fields.setId(rs.getLong("id"));
                fields.setField_name(rs.getString("field_name"));
                fields.setField_size(rs.getLong("field_size"));
                fields.setSize_unit(rs.getString("size_unit"));

                return fields;
            },ID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }

    }
}
