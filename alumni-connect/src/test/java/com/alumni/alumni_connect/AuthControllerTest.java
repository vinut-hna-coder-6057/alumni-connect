package com.alumni.alumni_connect;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void loginShouldWaitForApprovalWhenUserIsPending() {

        // Arrange

        UserRepository repository =
                Mockito.mock(UserRepository.class);

        BCryptPasswordEncoder encoder =
                Mockito.mock(BCryptPasswordEncoder.class);

        JwtUtil jwtUtil =
                Mockito.mock(JwtUtil.class);

        AuthController controller =
                new AuthController(
                        repository,
                        encoder
                );

        User existingUser =
                new User();

        existingUser.setEmail("student@gmail.com");
        existingUser.setRole("STUDENT");
        existingUser.setStatus("PENDING");
        existingUser.setPassword("encodedPassword");

        User loginUser =
                new User();

        loginUser.setEmail("student@gmail.com");
        loginUser.setRole("STUDENT");
        loginUser.setPassword("password123");

        when(
                repository.findByEmailAndRole(
                        "student@gmail.com",
                        "STUDENT"
                )
        ).thenReturn(
                Optional.of(existingUser)
        );

        // Act

        Object result =
                controller.login(loginUser);

        // Assert

        assertEquals(
                "WAIT_APPROVAL",
                result
        );
    }
}