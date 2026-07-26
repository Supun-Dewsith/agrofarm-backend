package com.agrofarm.api.repository;


import com.agrofarm.api.entity.UserEntity;

import java.util.Optional;

public interface UserRepository {
    Optional<UserEntity> findByUsername(String username);
    UserEntity save(UserEntity entity);
    Optional<UserEntity> findById(Long id);
}