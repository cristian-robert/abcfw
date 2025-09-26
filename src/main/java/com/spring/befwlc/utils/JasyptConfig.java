package com.spring.befwlc.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Configuration
public class JasyptConfig {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String TIMESTAMP = "1585333576093";
    private static final String DATE_FORMAT = "YYYYMM";
    private static final String ENCRYPT_TEMPLATE = "To encrypt: %s -> encrypted: %s";

//    @Value("${jasypt.encryptor.password}")
//    private String password;

    @Bean(name="jasyptStringEncryptor")
    public StringEncryptor stringEncryptor(){
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getDate());
        config.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC");
        config.setKeyObtentionIterations(1000);
        config.setProviderName("BC");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    private static String getDate(){
        final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        final Date time = new Date((Long.parseLong(TIMESTAMP)));
        return df.format(time);
    }
}
