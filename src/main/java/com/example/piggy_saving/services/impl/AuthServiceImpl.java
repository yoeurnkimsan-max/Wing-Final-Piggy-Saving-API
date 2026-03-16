package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.exception.RoleNotFoundExceptionHandler;
import com.example.piggy_saving.exception.UserAlreadyExistsException;
import com.example.piggy_saving.models.OtpVerificationModel;
import com.example.piggy_saving.models.RoleModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.UserRoleModel;
import com.example.piggy_saving.repository.OtpVerificationRepository;
import com.example.piggy_saving.repository.RoleRepository;
import com.example.piggy_saving.repository.UserRepository;
import com.example.piggy_saving.repository.UserRoleRepository;
import com.example.piggy_saving.security.CustomUserDetailsService;
import com.example.piggy_saving.security.JwtService;
import com.example.piggy_saving.services.AuthService;
import com.example.piggy_saving.services.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Inject PasswordEncoder directly
    private final CustomUserDetailsService customUserDetailsService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {

        log.info("Attempting to register user with email: {}", registerRequestDto.getEmail());

        // Check if email exists
        if (userRepository.existsUserModelByEmail(registerRequestDto.getEmail())) {
            log.warn("Email already exists: {}", registerRequestDto.getEmail());
            throw new UserAlreadyExistsException(
                    HttpStatus.BAD_REQUEST,
                    "User with email " + registerRequestDto.getEmail() + " already exists"
            );
        }

        // Check if phone exists
        if (userRepository.existsUserModelByPhone(registerRequestDto.getPhone_number())) {
            log.warn("Phone already exists: {}", registerRequestDto.getPhone_number());
            throw new UserAlreadyExistsException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number " + registerRequestDto.getPhone_number() + " already exists"
            );
        }

        RoleModel defaultRole = roleRepository.findByName("USER")
                .orElseThrow(()-> new RuntimeException("Default Role not found"));



        // Create and save user
        UserModel userModel = UserModel.builder()  // Use builder pattern
                .name(registerRequestDto.getFull_name())
                .email(registerRequestDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequestDto.getPassword()))
                .phone(registerRequestDto.getPhone_number())
                .emailVerified(false)
                .build();


        UserModel savedUser = userRepository.save(userModel);
        log.info("User saved successfully with ID: {}", savedUser.getId());


        UserRoleModel userRole = UserRoleModel.builder()
                .userId(savedUser.getId())
                .roleId(defaultRole.getId())
                .userModel(savedUser)
                .roleModel(defaultRole)
                .assignedAt(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);
        log.info("Registration successful for user: {}", savedUser.getEmail());

//        String OptCode = String.format("%06d", ThreadLocalRandom.current().nextInt(0,999999));
//        OtpVerificationModel otpVerificationModel = OtpVerificationModel.builder()
//                .phone(savedUser.getPhone())
//                .userModel(savedUser)
//                .attempts(3)
//                .optCode(OptCode)
//                .verified(false)
//                .expiresAt(LocalDateTime.now().plusMinutes(5))
//                .build();
//
//        otpVerificationRepository.save(otpVerificationModel);

        return RegisterResponseDto.builder()
                .status("PENDING")  // String, not integer
                .data(new RegisterResponseDto.UserData(savedUser.getId(), savedUser.getEmail(), null, null,0, 3000))
                .message("Registration successful. Please verify your phone via OTP sent to "+registerRequestDto.getPhone_number())
                .build();
    }
}