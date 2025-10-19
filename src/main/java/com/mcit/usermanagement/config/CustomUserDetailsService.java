package com.mcit.usermanagement.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.modal.User;

import com.mcit.usermanagement.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .flatMap(role -> {
                    // Add permissions for each role
                    Set<GrantedAuthority> authorities = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                            .collect(Collectors.toSet());

                    // Optionally add role name
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    return authorities.stream();
                })
                .collect(Collectors.toSet());
    }
}
