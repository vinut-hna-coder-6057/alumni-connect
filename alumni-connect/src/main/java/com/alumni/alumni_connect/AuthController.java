package com.alumni.alumni_connect;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Optional;

@RestController

@CrossOrigin(origins = "http://localhost:4200")

public class AuthController {
@Autowired
private JwtUtil jwtUtil;

    private final UserRepository repository;

    private final BCryptPasswordEncoder encoder;

    public AuthController(

            UserRepository repository,

            BCryptPasswordEncoder encoder

    ) {

        this.repository = repository;

        this.encoder = encoder;
    }

    // 🔹 SIGNUP

    @PostMapping("/signup")

    public String signup(
            @RequestBody User user
    ) {

        Optional<User> existing =

                repository.findByEmail(
                        user.getEmail()
                );

        // ❌ EMAIL EXISTS

        if (existing.isPresent()) {

            return "Email already exists";
        }

        // 🔐 ENCRYPT PASSWORD

        user.setPassword(

                encoder.encode(
                        user.getPassword()
                )
        );

        // ⏳ WAIT FOR APPROVAL

        user.setStatus("PENDING");

        repository.save(user);

        return "Signup successful";
    }

    // 🔹 LOGIN
    @PostMapping("/login")
public Object login(@RequestBody User user) {

    Optional<User> optionalUser =
            repository.findByEmailAndRole(
                    user.getEmail(),
                    user.getRole()
            );

    if (optionalUser.isEmpty()) {
        return "Invalid credentials";
    }

    User existing = optionalUser.get();

    if ("PENDING".equals(existing.getStatus())) {
        return "WAIT_APPROVAL";
    }

    if (!encoder.matches(
            user.getPassword(),
            existing.getPassword()
    )) {
        return "Invalid credentials";
    }

    return jwtUtil.generateToken(
            existing.getEmail(),
            existing.getRole()
    );
}

    // 🔹 APPROVE USER
// 🔹 APPROVE USER

@PutMapping("/approve/{id}")
@PreAuthorize("hasRole('ADMIN')")
public User approveUser(
        @PathVariable Long id
) {

    User user = repository
            .findById(id)
            .orElseThrow();

    user.setStatus("APPROVED");

    return repository.save(user);
}
