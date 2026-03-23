package com.example.piggy_saving.services.impl;

import com.example.piggy_saving.dto.request.LoginRequestDto;
import com.example.piggy_saving.dto.request.RegisterRequestDto;
import com.example.piggy_saving.dto.response.LoginResponseDto;
import com.example.piggy_saving.dto.response.RegisterResponseDto;
import com.example.piggy_saving.exception.OtpFailedToSentExceptionHandler;
import com.example.piggy_saving.exception.UserAlreadyExistsException;
import com.example.piggy_saving.mappers.AuthMapper;
import com.example.piggy_saving.models.RoleModel;
import com.example.piggy_saving.models.UserModel;
import com.example.piggy_saving.models.UserRoleModel;
import com.example.piggy_saving.repository.*;
import com.example.piggy_saving.security.CustomUserDetailsService;
import com.example.piggy_saving.security.JwtService;
import com.example.piggy_saving.services.AccountService;
import com.example.piggy_saving.services.AuthService;
import com.example.piggy_saving.services.EmailOtpService;
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
import java.util.Optional;

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
    private final EmailOtpService emailOtpService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AuthMapper authMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {

        log.info("Attempting to register user with email: {}", registerRequestDto.getEmail());


        // Check if email exists verify user
        if (userRepository.findUserModelByEmailAndEmailVerified(registerRequestDto.getEmail(), true).isPresent()) {
            log.warn("Email already exists: {}", registerRequestDto.getEmail());
            throw new UserAlreadyExistsException(
                    HttpStatus.BAD_REQUEST,
                    "User with this verify email " + registerRequestDto.getEmail() + " already exists"
            );
        }

        //check if email exists but not verify yet
        Optional<UserModel> existingUnverifiedUserEmail = userRepository.findUserModelByEmailAndEmailVerified(registerRequestDto.getEmail(), false);

        if (existingUnverifiedUserEmail.isPresent()) {
            UserModel user = existingUnverifiedUserEmail.get();
            // Send OTP email for verification
            boolean otpSent = emailOtpService.sendOtp(user.getEmail(), user.getName());
            if (!otpSent) {
                log.warn("Failed to send OTP email to: {}", user.getEmail());
                // Maybe throw exception or handle
                throw new OtpFailedToSentExceptionHandler(user.getEmail());
            }
            int otpExpiresIn = emailOtpService.getOtpInSeconds(LocalDateTime.now().plusMinutes(5));
            return RegisterResponseDto.builder()
                    .status("PENDING")
                    .message("Email already registered but not verified. New OTP sent.")
                    .data(
                            RegisterResponseDto.RegisterData.builder()
                                    .email(user.getEmail())
                                    .otpExpiresIn(otpExpiresIn)
                                    .build()
                    )
                    .build();
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
                .orElseThrow(() -> new RuntimeException("Default Role not found"));


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

        accountService.createMainAccount(savedUser);

        // Send OTP email for verification
        boolean otpSent = emailOtpService.sendOtp(savedUser.getEmail(), savedUser.getName());
        if (!otpSent) {
            log.warn("Failed to send OTP email to: {}", savedUser.getEmail());
            // Maybe throw exception or handle
            throw new OtpFailedToSentExceptionHandler(savedUser.getEmail());
        }

//        RegisterResponseDto registerResponseDto = authMapper.toRegisterResponseDto(savedUser);

        int otpExpiresIn = emailOtpService.getOtpInSeconds(LocalDateTime.now().plusMinutes(5));
        RegisterResponseDto.RegisterData registerData = RegisterResponseDto.RegisterData.builder()
                .email(savedUser.getEmail())
                .otpExpiresIn(otpExpiresIn)
                .build();


        RegisterResponseDto responseData = RegisterResponseDto.builder()
                .status("PENDING")
                .message("Registration successful. Please verify your email " + savedUser.getEmail() + " using the OTP sent. OTP will be expired in " + otpExpiresIn + "s.")
                .data(registerData)
                .build();

        return responseData;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting to login user with email: {}", loginRequestDto.getEmail());

        // Find user by email
        UserModel user = userRepository.findUserModelByEmail(loginRequestDto.getEmail());
        if (user == null) {
            log.warn("User not found with email: {}", loginRequestDto.getEmail());
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("Invalid email or password")
                    .build();
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            log.warn("User email not verified: {}", loginRequestDto.getEmail());
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("Please verify your email before logging in")
                    .build();
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", loginRequestDto.getEmail());
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("Invalid email or password")
                    .build();
        }

        // Generate JWT token with user roles and important claims
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequestDto.getEmail());

        // Get user roles
        List<UserRoleModel> userRoles = userRoleRepository.findByUserModel(user);
        List<String> roles = userRoles.stream()
                .map(userRole -> userRole.getRoleModel().getName())
                .toList();

        JwtService.TokenPair tokenPair = jwtService.generateTokenPairForUser(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailVerified(),
                roles,
                userDetails
        );

        log.info("Login successful for user: {}", user.getEmail());

        return LoginResponseDto.builder()
                .status("SUCCESS")
                .data(LoginResponseDto.LoginData.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .accessToken(tokenPair.accessToken)
                        .refreshToken(tokenPair.refreshToken)
                        .roles(roles)
                        .accessTokenExpiresIn(jwtService.getAccessTokenExpirationSeconds())
                        .tokenType("Bearer")
                        .build())
                .message("Login successful")
                .build();
    }

    @Override
    public LoginResponseDto verifyOtp(String email, String otpCode) {
        boolean verified = emailOtpService.verifyOtp(email, otpCode);

        if (!verified) {
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("Invalid or expired OTP")
                    .build();
        }
        UserModel user = userRepository.findUserModelByEmail(email);
        if (user == null) {
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("User not found")
                    .build();
        }

        // Generate JWT token with user roles and important claims
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Get user roles
        List<UserRoleModel> userRoles = userRoleRepository.findByUserModel(user);
        List<String> roles = userRoles.stream()
                .map(userRole -> userRole.getRoleModel().getName())
                .toList();

        JwtService.TokenPair tokenPair = jwtService.generateTokenPairForUser(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.isEmailVerified(),
                roles,
                userDetails
        );

        log.info("OTP verified successfully for email: {}", email);

        return LoginResponseDto.builder()
                .status("SUCCESS")
                .data(LoginResponseDto.LoginData.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .accessToken(tokenPair.accessToken)
                        .refreshToken(tokenPair.refreshToken)
                        .roles(roles)
                        .accessTokenExpiresIn(jwtService.getAccessTokenExpirationSeconds())
                        .tokenType("Bearer")
                        .build())
                .message("OTP verified successfully. Login token generated.")
                .build();
    }

    @Override
    public LoginResponseDto refreshToken(String refreshToken) {
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);

            // Load user details
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                return LoginResponseDto.builder()
                        .status("FAILED")
                        .message("Invalid refresh token")
                        .build();
            }

            // Get user from database
            UserModel user = userRepository.findUserModelByEmail(username);
            if (user == null) {
                return LoginResponseDto.builder()
                        .status("FAILED")
                        .message("User not found")
                        .build();
            }

            // Get user roles
            List<UserRoleModel> userRoles = userRoleRepository.findByUserModel(user);
            List<String> roles = userRoles.stream()
                    .map(userRole -> userRole.getRoleModel().getName())
                    .toList();

            // Generate new token pair
            JwtService.TokenPair tokenPair = jwtService.generateTokenPairForUser(
                    user.getId().toString(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.isEmailVerified(),
                    roles,
                    userDetails
            );

            log.info("Token refreshed successfully for user: {}", user.getEmail());

            return LoginResponseDto.builder()
                    .status("SUCCESS")
                    .data(LoginResponseDto.LoginData.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .accessToken(tokenPair.accessToken)
                            .refreshToken(tokenPair.refreshToken)
                            .roles(roles)
                            .accessTokenExpiresIn(jwtService.getAccessTokenExpirationSeconds())
                            .tokenType("Bearer")
                            .build())
                    .message("Token refreshed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return LoginResponseDto.builder()
                    .status("FAILED")
                    .message("Token refresh failed")
                    .build();
        }
    }
}
