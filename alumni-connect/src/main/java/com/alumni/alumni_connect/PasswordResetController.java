package com.alumni.alumni_connect;

import org.springframework.web.bind.annotation.*;

@RestController

@CrossOrigin(
        origins = "http://localhost:4200"
)

public class PasswordResetController {

    // =====================================
    // SERVICE
    // =====================================

    private final PasswordResetService
            passwordResetService;

    // =====================================
    // CONSTRUCTOR
    // =====================================

    public PasswordResetController(

            PasswordResetService
                    passwordResetService

    ) {

        this.passwordResetService =
                passwordResetService;
    }

    // =====================================
    // SEND OTP
    // =====================================

    @PostMapping(
            "/forgot-password"
    )

    public String forgotPassword(

            @RequestBody
            ForgotPasswordRequest request

    ) {

        return passwordResetService
                .forgotPassword(request);
    }

    // =====================================
    // VERIFY OTP
    // =====================================

    @PostMapping(
            "/verify-otp"
    )

    public String verifyOtp(

            @RequestBody
            VerifyOtpRequest request

    ) {

        return passwordResetService
                .verifyOtp(request);
    }

    // =====================================
    // RESET PASSWORD
    // =====================================

    @PostMapping(
            "/reset-password"
    )

    public String resetPassword(

            @RequestBody
            ResetPasswordRequest request

    ) {

        return passwordResetService
                .resetPassword(request);
    }
}
