package com.heraj.s3_client_side_encryption.util;

import com.heraj.s3_client_side_encryption.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

@Component
public class AesEncryption {

    private static final Logger logger = LoggerFactory.getLogger(AesEncryption.class);


    public void encryptFile(File inputFile, File outputFile, SecretKey secretKey) throws Exception {
        byte[] fileBytes = readFileToByteArray(inputFile);
        byte[] encryptedBytes = encrypt(fileBytes, secretKey);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(encryptedBytes);
        }
        logger.info("outputFile.length(): " + outputFile.length());
    }

    private byte[] readFileToByteArray(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }

    private byte[] encrypt(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV(); // Store IV for later use
        byte[] encryptedData = cipher.doFinal(data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        return byteBuffer.array();
    }

    public void decryptFile(File inputFile, File outputFile, SecretKey secretKey) throws Exception {
        byte[] encryptedBytes = readFileToByteArray(inputFile);
        byte[] decryptedBytes = decrypt(encryptedBytes, secretKey);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decryptedBytes);
        }
    }

    private byte[] decrypt(byte[] encryptedData, SecretKey secretKey) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[16]; // AES block size
        byteBuffer.get(iv);
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public static String generateChecksum(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Use SHA-256 for checksum
        try (InputStream is = new FileInputStream(file)) {
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
}

