package com.agrofarm.api.service;


import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userEntity.getRole().name()));

        if (userEntity.getPermissions() != null) {
            userEntity.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission))
            );
        }

        return AuthenticatedUser.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .authorities(authorities)
                .enabled(userEntity.getEnabled())
                .build();
    }
}
