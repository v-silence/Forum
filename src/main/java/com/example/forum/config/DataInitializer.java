package com.example.forum.config;

import com.example.forum.model.Role;
import com.example.forum.model.UserAccount;
import com.example.forum.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            userRepository.save(createUser("admin", "admin@student.local", "admin123", Set.of(Role.ROLE_ADMIN, Role.ROLE_MODERATOR, Role.ROLE_USER), passwordEncoder));
            userRepository.save(createUser("moderator", "moderator@student.local", "mod12345", Set.of(Role.ROLE_MODERATOR, Role.ROLE_USER), passwordEncoder));
            userRepository.save(createUser("student", "student@student.local", "student123", Set.of(Role.ROLE_USER), passwordEncoder));
        };
    }

    private UserAccount createUser(String username, String email, String password, Set<Role> roles, PasswordEncoder passwordEncoder) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoles(roles);
        return user;
    }
}
