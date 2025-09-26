package com.spring.befwlc.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.security.Security;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class EncryptionUtils {
    private static final String TIMESTAMP = "1585333576093";
    private static final String DATE_FORMAT = "YYYYMM";
    private static final String ENCRYPT_TEMPLATE = "To encrypt: %s -> encrypted: %s";

    public static void main(final String[] args) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        if(args.length != 2){
            throw new Exception("Two parameters must be specified: operation and string to encrypt/decrypt (e.g. encrypt mypassword)");
        }

        final String command = String.valueOf(args[0]);
        if(command.equals("encrypt")){
            console(encrypt(String.valueOf(args[1])));
        } else if (command.equals("decrypt")) {
            console(decrypt(String.valueOf(args[1])));
        } else {
            throw new Exception("Unknown command: " + command);
        }
    }

    private static void console(final String message){
        System.out.println(message);
    }

    public static String decrypt(final String target){
        return stringEncryptor().encrypt(target);
    }

    private static String encrypt(final String target){
        return String.format(ENCRYPT_TEMPLATE, target, stringEncryptor().encrypt(target));
    }

    private static StringEncryptor stringEncryptor(){
        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        final SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getDate());
        config.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC");
        config.setKeyObtentionIterations("1000");
        config.setProvider(new BouncyCastleProvider());
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
