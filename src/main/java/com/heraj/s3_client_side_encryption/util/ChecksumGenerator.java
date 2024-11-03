package com.heraj.s3_client_side_encryption.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class ChecksumGenerator {
    public static String generateChecksum(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Change to SHA-1, MD5, etc. if needed
        try (InputStream is = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] checksumBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : checksumBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) {
        try {
            String filePath = System.getProperty("user.home");// Replace with your file path
            System.out.println(filePath);
            String checksum = generateChecksum(filePath);
            System.out.println("Checksum: " + checksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
