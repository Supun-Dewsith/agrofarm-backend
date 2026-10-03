package com.agrofarm.api.service;

import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FarmAccessService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public UserEntity currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in"
            );
        }

        UserEntity user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        if (!Boolean.TRUE.equals(user.getEnabled())
                || user.getRole() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Account is not active"
            );
        }

        return user;
    }

    public void requireAdmin(UserEntity user) {

        if (user.getRole() != Role.ADMIN) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can perform this action"
            );
        }
    }

    public void requireRead(
            UserEntity user,
            Long farmId
    ) {

        if (farmId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Farm is required"
            );
        }

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM farm_assignments
                WHERE user_id = ?
                  AND farm_id = ?
                """,
                Long.class,
                user.getId(),
                farmId
        );

        if (count == null || count == 0) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not assigned to this farm"
            );
        }
    }

    public void requireManageFields(
            UserEntity user,
            Long farmId
    ) {

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.SUPERVISOR) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators and assigned supervisors can manage fields"
            );
        }

        requireRead(user, farmId);
    }
}