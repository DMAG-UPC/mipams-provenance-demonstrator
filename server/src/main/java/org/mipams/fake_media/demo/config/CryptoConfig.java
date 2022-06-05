package org.mipams.fake_media.demo.config;

import org.mipams.jumbf.crypto.services.CryptoService;
import org.mipams.jumbf.crypto.services.KeyReaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CryptoConfig {
    @Bean
    public KeyReaderService keyReaderService() {
        return new KeyReaderService();
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
