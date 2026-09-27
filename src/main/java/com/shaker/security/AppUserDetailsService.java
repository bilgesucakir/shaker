package com.shaker.security;

import com.shaker.entity.user.User;
import com.shaker.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AppUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public AppUserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        return new AppUserPrincipal(user);
    }
}
