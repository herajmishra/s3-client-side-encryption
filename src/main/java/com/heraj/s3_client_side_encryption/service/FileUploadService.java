package com.heraj.s3_client_side_encryption.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.heraj.s3_client_side_encryption.util.AesEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
//import java.io;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final AmazonS3 s3Client;

    @Autowired
    private AesEncryption aesEncryption;

    @Value("${aws.s3.bucket.name}") // Corrected the syntax here
    private String bucket;

    @Value("${spring.servlet.multipart.location}")
    private String multipartLocation;

    public FileUploadService(@Value("${aws.upload.accessKey}") String accessKey,
                             @Value("${aws.upload.secretKey}") String secretKey,
                             @Value("${aws.s3.region}") String region) {

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }

    public void uploadFile(MultipartFile multipartFile, SecretKey secretKey) throws Exception {
        // Encrypt the file

        Path tempDir = Paths.get(multipartLocation);
        Path tempFilePath = tempDir.resolve(multipartFile.getOriginalFilename());
        File tempInputFile = File.createTempFile(tempFilePath + "inputs-", ".tmp");
        File tempEncryptedFile = File.createTempFile(tempFilePath + "encrypteds-", ".tmp");

        try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
            fos.write(multipartFile.getBytes());
        }

//        String originalChecksum = AesEncryption.generateChecksum(tempInputFile);


        logger.info("uploadFile - aes");
        aesEncryption.encryptFile(tempInputFile, tempEncryptedFile, secretKey);
        logger.info("uploadFile - aes1");
        // Upload file to S3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        //metadata.setContentLength(multipartFile.getSize());


        InputStream inputStream = new FileInputStream(tempEncryptedFile);
        try {
            // Now you can use inputStream to read data from the file
            int data = inputStream.read();
            while (data != -1) {
                System.out.print((char) data); // example processing
                data = inputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("uploadefile - uploadeding");


        s3Client.putObject(
                new PutObjectRequest(bucket, multipartFile.getOriginalFilename(), tempEncryptedFile)
        );

        logger.info("uploadefile - uploaded...");
        // Generate and store checksum
        File tempFile = convertMultipartFileToFile(multipartFile);
        String originalChecksum = generateAndStoreChecksum(tempFile, multipartFile.getOriginalFilename());

        System.out.println("Original Checksum: " + originalChecksum);
    }

    public File multipartFileToFile(MultipartFile file) throws IOException {
        return convertMultipartFileToFile(file);
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        Path tempDir = Paths.get(multipartLocation);
        Path tempFilePath = tempDir.resolve(file.getOriginalFilename());
        File tempFile = tempFilePath.toFile();
        file.transferTo(tempFile);
        return tempFile;
    }

    public String generateAndStoreChecksum(File file, String fileName) throws Exception {
        logger.info("generateAndStoreChecksum");
        String checksum = AesEncryption.generateChecksum(file); // Use the utility we defined earlier
        saveChecksumInProperties(fileName, checksum);
        return checksum;
    }

    private void saveChecksumInProperties(String fileName, String checksum) throws Exception {
        Properties properties = new Properties();
        File propertiesFile = new File("src/main/resources/checksums.properties");

        if (propertiesFile.exists()) {
            try (InputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
            }
        }

        properties.setProperty(fileName, checksum);
        try (OutputStream output = new FileOutputStream(propertiesFile)) {
            properties.store(output, null);
        }
    }
}
