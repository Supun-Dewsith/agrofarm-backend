package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.crops.*;
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




    public String findRoleByUsername(String currentUsername){

        String sql = "SELECT role FROM users WHERE user_name = ?";
        try {
            return template.queryForObject(sql, (rs, rowNum) -> {

                String role = rs.getString("role");

                return role;
            },currentUsername);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }

    }

    @Override
    public String findFarmNameByFarmID(Long farmID){

        String sql = "SELECT farm_name FROM farms WHERE id = ?";
        try {
            return template.queryForObject(sql, (rs, rowNum) -> {

                String farmname = rs.getString("farm_name");

                return farmname;
            },farmID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }
    }

    @Override
    public List<FarmIDs> findFarmIdsFromUserID(Long ID) {

        String sql = """
        SELECT farm_id
        FROM farm_assignments
        WHERE user_id = ?
        """;

        try {
            return template.query(sql, (rs, rowNum) -> {

                FarmIDs IDs = new FarmIDs();

                Long farmID = (rs.getLong("farm_id"));

                IDs.setFarmIDs(rs.getLong("farm_id"));

                String farmname = findFarmNameByFarmID(farmID);
                IDs.setFarmNames(farmname);


                return IDs;

            }, ID);

        } catch (EmptyResultDataAccessException e) {
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



    @Override
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


    public String findFieldNameByFieldID(Long feildID){

        String fieldName = template.queryForObject(
                "SELECT field_name FROM fields WHERE id = ? ",
                String.class,
                feildID
        );

        return fieldName;
    }

    public String findAreaByFieldID(Long fieldID){

        String sql = "SELECT field_size, size_unit FROM fields WHERE id = ?";
        try {
            return template.queryForObject(sql, (rs, rowNum) -> {

                Double fieldSize = rs.getDouble("field_size");
                String sizeUnit = rs.getString("size_unit");

                String area = String.valueOf(fieldSize) + " " + sizeUnit;

                return area;
            },fieldID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }
    }


    @Override
    public List<CropDetails> findCropDetailsFromFarmID(Long farmID){

        String sql = """
                SELECT
                    crop_id,
                    crop_name,
                    variety,
                    field_id,
                    growth_stage,
                    progress,
                    status,
                    plant_date,
                    harvest_date,
                    crop_image_url,
                    plant_date,
                    harvest_date 
                FROM crops 
                WHERE farm_id = ?
                """;
        try {
            return template.query(sql, (rs, rowNum) -> {

                CropDetails crops = new CropDetails();

                crops.setId(rs.getLong("crop_id"));
                crops.setName(rs.getString("crop_name"));
                crops.setVariety(rs.getString("variety"));

                // get field ID
                Long fieldId = rs.getLong("field_id");

                //find the field name
                String fieldName = findFieldNameByFieldID(fieldId);

                crops.setField(fieldName);
                crops.setImage(rs.getString("crop_image_url"));


                String area = findAreaByFieldID(fieldId);
                crops.setArea(area);
                crops.setGrowthStage(rs.getString("growth_stage"));
                crops.setProgress(rs.getInt("progress"));
                crops.setStatus(rs.getString("status"));
                crops.setPlantDate(rs.getTimestamp("plant_date").toLocalDateTime());
                crops.setHarvestDate(rs.getTimestamp("harvest_date").toLocalDateTime());
                return crops;
            },farmID);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }

    }


    @Override
    public void deleteCrop(Long cropId) {
        String sql = """
            DELETE FROM crops
            WHERE crop_id = ?
            """;

        template.update(sql, cropId);
    }



    @Override
    public void updateCrop(Long cropId, NewCropRequest cropDTO) {

        String sql = """
            UPDATE crops
            SET crop_name = ?,
                variety = ?,
                farm_id = ?,
                field_id = ?,
                growth_stage = ?,
                progress = ?,
                status = ?,
                plant_date = ?,
                harvest_date = ?
            WHERE crop_id = ?
            """;

        template.update(
                sql,
                cropDTO.getCropName(),
                cropDTO.getVariety(),
                cropDTO.getFarmId(),
                cropDTO.getFieldId(),
                cropDTO.getGrowthStage(),
                cropDTO.getProgress(),
                cropDTO.getStatus(),
                cropDTO.getPlantDate(),
                cropDTO.getHarvestDate(),
                cropId
        );
    }


    @Override
    public CropUpdateDetails findCropById(Long cropId) {

        String sql = """
        SELECT
            crop_id,
            crop_name,
            variety,
            farm_id,
            field_id,
            growth_stage,
            progress,
            status,
            plant_date,
            harvest_date,
            crop_image_url
        FROM crops
        WHERE crop_id = ?
        """;

        return template.queryForObject(sql, (rs, rowNum) -> {

            CropUpdateDetails crop = new CropUpdateDetails();

            crop.setId(rs.getLong("crop_id"));
            crop.setName(rs.getString("crop_name"));
            crop.setVariety(rs.getString("variety"));
            crop.setFarmId(rs.getLong("farm_id"));
            crop.setFieldId(rs.getLong("field_id"));
            crop.setGrowthStage(rs.getString("growth_stage"));
            crop.setProgress(rs.getInt("progress"));
            crop.setStatus(rs.getString("status"));
            crop.setImage(rs.getString("crop_image_url"));

            if (rs.getTimestamp("plant_date") != null) {
                crop.setPlantDate(
                        rs.getTimestamp("plant_date").toLocalDateTime()
                );
            }

            if (rs.getTimestamp("harvest_date") != null) {
                crop.setHarvestDate(
                        rs.getTimestamp("harvest_date").toLocalDateTime()
                );
            }

            return crop;

        }, cropId);
    }



    @Override
    public List<FarmIDs> findAdminFarmIdsFromUserID(Long id){

        String sql = "SELECT id, farm_name FROM farms WHERE owner_id = ?";
        try {
            return template.query(sql, (rs, rowNum) -> {
                FarmIDs IDs = new FarmIDs();
                IDs.setFarmIDs(rs.getLong("id"));
                IDs.setFarmNames(rs.getString("farm_name"));
                return IDs;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user return null; }
            return null;
        }
    }
}
