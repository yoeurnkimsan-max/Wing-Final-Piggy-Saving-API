package com.example.piggy_saving.config;

import com.example.piggy_saving.models.RoleModel;
import com.example.piggy_saving.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if the "USER" role already exists
        if (roleRepository.findByName("USER").isEmpty()) {
            RoleModel userRole = RoleModel.builder()
                    .name("USER")
                    .description("Default user role")
                    .build();
            roleRepository.save(userRole);
            log.info("Default USER role created.");
        } else {
            log.info("USER role already exists.");
        }

        if(roleRepository.findByName("ADMIN").isEmpty()){
            RoleModel userRole = RoleModel.builder()
                    .name("ADMIN")
                    .description("Default admin role")
                    .build();
            roleRepository.save(userRole);
            log.info("Default ADMIN role created.");
        }else {
            log.info("ADMIN role already exists.");
        }
        // You can add more default roles here if needed (e.g., ADMIN)
    }
}