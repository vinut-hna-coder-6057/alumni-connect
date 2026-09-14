
package com.alumni.alumni_connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository
        extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByEmailOrderByIdDesc(
            String email
    );

    void deleteByEmail(String email);
}
