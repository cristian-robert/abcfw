package com.spring.befwlc.v2.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.security.Security;

public class EncryptionHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: EncryptionHelper <encrypt|decrypt> <value> [key]");
            System.out.println("  If key is not provided, uses ENCRYPTION_KEY env var or default dev key");
            return;
        }

        String action = args[0];
        String value = args[1];
        String key = args.length > 2 ? args[2]
                : System.getenv().getOrDefault("ENCRYPTION_KEY", "defaultDevKey2024");

        TextEncryptor encryptor = Encryptors.text(key, "deadbeef");

        if ("encrypt".equalsIgnoreCase(action)) {
            String encrypted = encryptor.encrypt(value);
            System.out.println("Encrypted: {cipher}" + encrypted);
            System.out.println("\nPaste this into your application.yml:");
            System.out.println("  property-name: '{cipher}" + encrypted + "'");
        } else if ("decrypt".equalsIgnoreCase(action)) {
            String cleaned = value.replace("{cipher}", "");
            String decrypted = encryptor.decrypt(cleaned);
            System.out.println("Decrypted: " + decrypted);
        } else {
            System.out.println("Unknown action: " + action + ". Use 'encrypt' or 'decrypt'.");
        }
    }
}
