package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import com.agrofarm.api.dto.crops.NewCropRequest;
import com.agrofarm.api.repository.CropRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

        String sql = "SELECT id, field_name, field_size, size_unit FROM fields WHERE farm_id = ?";
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


    @Override
    public Long insertNewCrop (NewCropRequest cropDTO){

        String sql = """
                INSERT INTO crops (
                crop_name,variety,farm_id,field_id,growth_stage,progress,status, plant_date,harvest_date
                ) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, cropDTO.getCropName());
            ps.setString(2, cropDTO.getVariety());
            ps.setLong(3, cropDTO.getFarmId());
            ps.setLong(4, cropDTO.getFieldId());
            ps.setString(5, cropDTO.getGrowthStage());
            ps.setInt(6, (Integer) cropDTO.getProgress());
            ps.setString(7, cropDTO.getStatus());

            ps.setObject(8, cropDTO.getPlantDate());
            ps.setObject(9, cropDTO.getHarvestDate());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    public void insertImage(String image_url, Long cropId){

        String sql = """
        UPDATE crops 
        SET crop_image_url = ?
        WHERE crop_id = ?
        """;

        template.update(
                sql,
                image_url,
                cropId
        );



    }
}
