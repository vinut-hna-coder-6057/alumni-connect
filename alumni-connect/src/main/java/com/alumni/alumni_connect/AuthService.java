package com.alumni.alumni_connect;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository repository;

    private final BCryptPasswordEncoder encoder;

    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository repository,
            BCryptPasswordEncoder encoder,
            JwtUtil jwtUtil
    ) {

        this.repository = repository;

        this.encoder = encoder;

        this.jwtUtil = jwtUtil;
    }

    // =====================================
    // SIGNUP
    // =====================================

    public String signup(User user) {

        Optional<User> existing =
                repository.findByEmail(
                        user.getEmail()
                );

        if (existing.isPresent()) {

            return "Email already exists";
        }

        // ENCRYPT PASSWORD

        user.setPassword(
                encoder.encode(
                        user.getPassword()
                )
        );

        // WAIT FOR ADMIN APPROVAL

        user.setStatus("PENDING");

        repository.save(user);

        return "Signup successful";
    }

    // =====================================
    // LOGIN
    // =====================================

    public Object login(User user) {

        Optional<User> optionalUser =
                repository.findByEmailAndRole(
                        user.getEmail(),
                        user.getRole()
                );

        if (optionalUser.isEmpty()) {

            return "Invalid credentials";
        }

        User existing =
                optionalUser.get();

        // CHECK APPROVAL

        if ("PENDING".equals(
                existing.getStatus()
        )) {

            return "WAIT_APPROVAL";
        }

        // CHECK PASSWORD

        if (!encoder.matches(
                user.getPassword(),
                existing.getPassword()
        )) {

            return "Invalid credentials";
        }

        // GENERATE JWT

        return jwtUtil.generateToken(
                existing.getEmail(),
                existing.getRole()
        );
    }

    // =====================================
    // APPROVE USER
    // =====================================

    @PreAuthorize("hasRole('ADMIN')")
    public User approveUser(Long id) {

        User user =
                repository
                        .findById(id)
                        .orElseThrow();

        user.setStatus("APPROVED");

        return repository.save(user);
    }
}
