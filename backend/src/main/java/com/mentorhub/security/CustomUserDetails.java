package com.mentorhub.security;

import com.mentorhub.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/**
 * CustomUserDetails - wraps our User entity so the authenticated principal
 * carries the internal numeric user ID, not just the email.
 *
 * WHY THIS EXISTS:
 * Spring Security's default org.springframework.security.core.userdetails.User
 * only stores username (email) + password + authorities. Controllers had no
 * server-side way to know "who is actually making this request" other than
 * trusting a client-supplied {userId}/{teacherId} in the URL — which is
 * exactly what let one student read or edit another student's data. Carrying
 * the id here lets SecurityUtils.getCurrentUserId() resolve the real,
 * token-verified identity for every request.
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
