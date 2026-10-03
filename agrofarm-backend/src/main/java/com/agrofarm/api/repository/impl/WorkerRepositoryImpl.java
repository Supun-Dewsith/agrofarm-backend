package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.workers.RequestPartOne;
import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.repository.CropRepository;
import com.agrofarm.api.repository.RefreshTokenRepository;
import com.agrofarm.api.repository.WorkerRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkerRepositoryImpl implements WorkerRepository{
    private final JdbcTemplate template;

    CropRepository cropRepository;

    public WorkerRepositoryImpl(JdbcTemplate template, CropRepository cropRepository){

        this.template = template;
        this.cropRepository = cropRepository;    }




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



    @Override
    public List<Long> findWorkerIdByFarmId(Long farmId) {

        String sql = """
            SELECT user_id FROM farm_assignments WHERE farm_id = ?

            """;

        return template.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                farmId

        );
    }




    @Override
    public Long findFarmIdFromUserID(Long id){

        String sql = """
            SELECT farm_id FROM farm_assignments WHERE user_id = ?

            """;

        return template.queryForObject(
                sql,
                (rs, rowNum) -> rs.getLong("farm_id"),
                id

        );
    }

    @Override
    public RequestPartOne findnamePhoneEmailFarmJoindateImage(Long id){

        String sql = "SELECT user_name, phone_number, email, created_at, image_url, role FROM users WHERE id = ?";
        try {
            return template.queryForObject(sql, (rs, rowNum) -> {
                RequestPartOne requestPartOne = new RequestPartOne();
                requestPartOne.setName(rs.getString("user_name"));
                requestPartOne.setPhone(rs.getString("phone_number"));
                requestPartOne.setEmail(rs.getString("email"));
                requestPartOne.setJoinDate(rs.getString("created_at"));
                requestPartOne.setProfile_picture(rs.getString("image_url"));
                requestPartOne.setRole(rs.getString("role"));
                return requestPartOne;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user return null; }
            return null;
        }

    }

    public String findTaskNameFromTaskID(Long id){

        String sql = """
            SELECT title FROM tasks WHERE id = ?

            """;

        return template.queryForObject(
                sql,
                (rs, rowNum) -> rs.getString("title"),
                id

        );

    }



    @Override
    public List<String> findTasksFromUserID(Long id){

        String sql = """
            SELECT task_id FROM task_assignments WHERE worker_id = ?

            """;

        try {
            return template.query(sql, (rs, rowNum) -> {
                Long taskID = rs.getLong("task_id");

                String task_title = findTaskNameFromTaskID(taskID);
                return task_title;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user return null; }
            return null;
        }
    }



    @Override
    public String findStatusFromUserId(Long id){

        String sql = """
            SELECT status FROM attendance WHERE user_id = ?

            """;

        try {
            return template.queryForObject(
                    sql,
                    (rs, rowNum) -> rs.getString("status"),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return "Inactive";
        }
    }





    @Override
    public List<FarmIDs> findSupervisorFarmIdsFromUserID(Long id) {

        String sql = """
        SELECT f.id, f.farm_name
        FROM farms f
        JOIN farm_assignments fa ON fa.farm_id = f.id
        WHERE fa.user_id = ?
        """;

        return template.query(sql, (rs, rowNum) -> {
            FarmIDs IDs = new FarmIDs();
            IDs.setFarmIDs(rs.getLong("id"));
            IDs.setFarmNames(rs.getString("farm_name"));
            return IDs;
        }, id);
    }




    @Override
    public int deleteWorker(Long id){
        String sql = "DELETE FROM users WHERE id = ?";

        return template.update(sql, id);
    }

}
