package com.alumni.alumni_connect;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {

        this.otpRepository = otpRepository;
    }

    // =====================================
    // GENERATE OTP
    // =====================================

    public String generateOtp(String email) {

        Random random = new Random();

        int number =
                100000 + random.nextInt(900000);

        String otp =
                String.valueOf(number);

        // REMOVE PREVIOUS OTP
        otpRepository.deleteByEmail(email);

        // CREATE NEW OTP
        Otp otpEntity = new Otp();

        otpEntity.setEmail(email);

        otpEntity.setOtp(otp);

        otpEntity.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpEntity.setVerified(false);

        // SAVE TO MYSQL
        otpRepository.save(otpEntity);

        System.out.println(
                "OTP GENERATED FOR "
                        + email
                        + ": "
                        + otp
        );

        return otp;
    }

    // =====================================
    // VERIFY OTP
    // =====================================

    public boolean verifyOtp(
            String email,
            String otp
    ) {

        // FIND LATEST OTP
        Optional<Otp> optionalOtp =
                otpRepository
                        .findTopByEmailOrderByIdDesc(email);

        // OTP DOES NOT EXIST
        if (optionalOtp.isEmpty()) {

            return false;
        }

        Otp otpEntity =
                optionalOtp.get();

        // CHECK EXPIRY
        if (
                otpEntity.getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            otpRepository.deleteById(
                    otpEntity.getId()
            );

            return false;
        }

        // CHECK OTP
        if (
                !otpEntity.getOtp()
                        .equals(otp)
        ) {

            return false;
        }

        // MARK AS VERIFIED
        otpEntity.setVerified(true);

        otpRepository.save(otpEntity);

        return true;
    }
}
