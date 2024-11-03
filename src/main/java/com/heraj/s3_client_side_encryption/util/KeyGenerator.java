package com.heraj.s3_client_side_encryption.util;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        byte[] key = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(key);
        String encodedKey = Base64.getEncoder().encodeToString(key);
        System.out.println(encodedKey); // Print and copy this value to application.properties
    }
}
