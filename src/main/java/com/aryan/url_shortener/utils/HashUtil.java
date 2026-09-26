package com.aryan.url_shortener.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    
    public static String sha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for(byte b : hash){
                String hex = Integer.toHexString(0xff & b);//convert to hexadecimal

                if(hex.length() == 1){
                    hexString.append("0");
                }//make sur every byte represent two characters

                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
           throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
