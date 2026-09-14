package com.alumni.alumni_connect;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(
            OtpRepository otpRepository
    ) {

        this.otpRepository =
                otpRepository;
    }

    // =====================================
    // GENERATE OTP
    // =====================================

    public String generateOtp(
            String email
    ) {

        Random random = new Random();

        int number =
                100000 + random.nextInt(900000);

        String otpValue =
                String.valueOf(number);

        // REMOVE OLD OTP

        otpRepository.deleteByEmail(
                email
        );

        // CREATE NEW OTP

        Otp otp =
                new Otp();

        otp.setEmail(email);

        otp.setOtp(otpValue);

        otp.setExpiry(
                LocalDateTime.now()
                        .plusMinutes(5)
        );

        otp.setVerified(false);

        otpRepository.save(otp);

        return otpValue;
    }

    // =====================================
    // VERIFY OTP
    // =====================================

    public boolean verifyOtp(
            String email,
            String otpValue
    ) {

        Otp otp =
                otpRepository
                        .findByEmail(email)
                        .orElse(null);

        // OTP NOT FOUND

        if (otp == null) {

            return false;
        }

        // CHECK EXPIRY

        if (
                otp.getExpiry()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            otpRepository.deleteByEmail(
                    email
            );

            return false;
        }

        // CHECK OTP

        if (
                !otp.getOtp()
                        .equals(otpValue)
        ) {

            return false;
        }

        // MARK VERIFIED

        otp.setVerified(true);

        otpRepository.save(otp);

        return true;
    }
}
