package com.mentorhub.repository;

import com.mentorhub.entity.EmailVerificationToken;
import com.mentorhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    /**
     * All tokens previously issued to this user — used to invalidate old,
     * unused tokens when a fresh one is issued (on resend), so only the
     * newest link can ever work.
     */
    List<EmailVerificationToken> findByUserAndUsedFalse(User user);
}
