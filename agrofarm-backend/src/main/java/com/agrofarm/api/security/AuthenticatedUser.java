package com.agrofarm.api.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthenticatedUser implements UserDetails {
    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<String> permissions;
    private final String imageUrl;

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}