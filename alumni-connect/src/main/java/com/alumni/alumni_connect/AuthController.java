package com.alumni.alumni_connect;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {

        this.authService = authService;
    }

    // =====================================
    // SIGNUP
    // =====================================

    @PostMapping("/signup")
    public String signup(
            @RequestBody User user
    ) {

        return authService.signup(user);
    }

    // =====================================
    // LOGIN
    // =====================================

    @PostMapping("/login")
    public Object login(
            @RequestBody User user
    ) {

        return authService.login(user);
    }

    // =====================================
    // APPROVE USER
    // =====================================

    @PutMapping("/approve/{id}")
    public User approveUser(
            @PathVariable Long id
    ) {

        return authService.approveUser(id);
    }
}
