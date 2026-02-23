package com.spring.befwlc.v2.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.security.Security;

@Configuration
public class EncryptionConfig {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public TextEncryptor textEncryptor() {
        String key = System.getenv().getOrDefault("ENCRYPTION_KEY", "defaultDevKey2024");
        String salt = "deadbeef";
        return Encryptors.text(key, salt);
    }
}
