package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByFarmId(Long farmId);

    @Query(
            value = """
                    SELECT f.*
                    FROM fields f
                    WHERE EXISTS (
                        SELECT 1
                        FROM farm_assignments fa
                        WHERE fa.farm_id = f.farm_id
                          AND fa.user_id = :userId
                    )
                    """,
            nativeQuery = true
    )
    List<Field> findAssignedToUser(
            @Param("userId") Long userId
    );
}