package com.doan2025.webtoeic.repository;

import com.doan2025.webtoeic.domain.ForgotPassword;
import com.doan2025.webtoeic.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {

    @Query("""
        SELECT fp from ForgotPassword fp where fp.user = :user and fp.otp = :otp
""")
    Optional<ForgotPassword> findByOtpAndUser(@Param("otp") Integer otp,@Param("user") User user);

    boolean existsByUser(User user);

    void deleteByUser(User user);
}
