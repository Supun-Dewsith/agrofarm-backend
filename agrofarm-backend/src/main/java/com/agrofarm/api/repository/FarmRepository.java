package com.agrofarm.api.repository;

import com.agrofarm.api.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findByOwnerId(Long ownerId);

    @Query(
            value = """
                    SELECT f.*
                    FROM farms f
                    WHERE EXISTS (
                        SELECT 1
                        FROM farm_assignments fa
                        WHERE fa.farm_id = f.id
                          AND fa.user_id = :userId
                    )
                    """,
            nativeQuery = true
    )
    List<Farm> findAssignedToUser(
            @Param("userId") Long userId
    );
}