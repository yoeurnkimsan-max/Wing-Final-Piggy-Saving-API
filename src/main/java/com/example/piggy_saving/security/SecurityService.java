package com.example.piggy_saving.security;

import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    UserRepository userRepository;

    public boolean isEmailVerified(Authentication authentication){
        if(authentication == null || !(authentication.getPrincipal() instanceof UserDetails)){
            return false;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        UserModel user = userRepository.findUserModelByEmailAndEmailVerified(userDetails.getUsername(), true).orElse(null);
        return user != null;
    }
}
