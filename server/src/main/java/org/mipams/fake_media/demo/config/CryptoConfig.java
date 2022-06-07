package org.mipams.fake_media.demo.config;

import org.mipams.jumbf.crypto.services.CredentialsReaderService;
import org.mipams.jumbf.crypto.services.CryptoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CryptoConfig {
    @Bean
    public CredentialsReaderService credentialsReaderService() {
        return new CredentialsReaderService();
    }

    @Bean
    public CryptoService cryptoService() {
        return new CryptoService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
