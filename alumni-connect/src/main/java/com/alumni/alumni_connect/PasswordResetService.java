package com.alumni.alumni_connect;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final OtpRepository otpRepository;

    public PasswordResetService(
            UserRepository userRepository,
            OtpService otpService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            OtpRepository otpRepository
    ) {

        this.userRepository =
                userRepository;

        this.otpService =
                otpService;

        this.emailService =
                emailService;

        this.passwordEncoder =
                passwordEncoder;

        this.otpRepository =
                otpRepository;
    }

    // =====================================
    // SEND OTP
    // =====================================

    public String forgotPassword(
            ForgotPasswordRequest request
    ) {

        User user =
                userRepository.findByEmail(
                        request.getEmail()
                ).orElse(null);

        if (user == null) {

            return "User not found";
        }

        String otp =
                otpService.generateOtp(
                        request.getEmail()
                );

        emailService.sendOtpEmail(
                request.getEmail(),
                otp
        );

        return "OTP sent successfully";
    }

    // =====================================
    // VERIFY OTP
    // =====================================

    public String verifyOtp(
            VerifyOtpRequest request
    ) {

        boolean valid =
                otpService.verifyOtp(
                        request.getEmail(),
                        request.getOtp()
                );

        if (!valid) {

            return "Invalid or expired OTP";
        }

        return "OTP verified";
    }

    // =====================================
    // RESET PASSWORD
    // =====================================

    public String resetPassword(
            ResetPasswordRequest request
    ) {

        // CHECK OTP WAS VERIFIED

        boolean otpVerified =
                otpRepository
                        .findByEmailAndVerifiedTrue(
                                request.getEmail()
                        )
                        .isPresent();

        if (!otpVerified) {

            return "OTP verification required";
        }

        // FIND USER

        User user =
                userRepository.findByEmail(
                        request.getEmail()
                ).orElse(null);

        if (user == null) {

            return "User not found";
        }

        // ENCODE NEW PASSWORD

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        // SAVE USER

        userRepository.save(user);

        // CONSUME VERIFIED OTP

        otpRepository.deleteByEmail(
                request.getEmail()
        );

        return "Password reset successful";
    }
}
