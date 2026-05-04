package com.eldercare.service.service;

import com.eldercare.service.repository.InvalidatedTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupScheduler {

    private static final Logger log = LogManager.getLogger(TokenCleanupScheduler.class);

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${jwt.expiration.ms}")
    private long expirationMs;

    public TokenCleanupScheduler(InvalidatedTokenRepository invalidatedTokenRepository) {
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Scheduled(cron = "${app.token.cleanup.cron}")
    public void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(expirationMs / 1000);
        invalidatedTokenRepository.deleteExpiredTokens(cutoff);
        log.info("Cleaned up invalidated tokens older than {}", cutoff);
    }
}
