package com.example.piggy_saving.security;

import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Use eager fetch to avoid LazyInitializationException
        String[] roles = userRoleRepository.findByUserModelWithRoles(user)
                .stream()
                .map(r -> r.getRoleModel().getName())
                .toArray(String[]::new);

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(roles)
                .build();
    }
}
