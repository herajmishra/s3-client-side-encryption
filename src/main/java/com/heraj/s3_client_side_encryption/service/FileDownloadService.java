package com.heraj.s3_client_side_encryption.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Service
public class FileDownloadService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private Environment env;

    private final String bucketName = "ccs-lab8";

    public FileDownloadService(@Value("${aws.accessKey}") String accessKey,
                               @Value("${aws.secretKey}") String secretKey,
                               @Value("${aws.s3.region}") String region) {

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.amazonS3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }

    public File downloadFile(String fileName) throws Exception {
        File tempFile = File.createTempFile("download-", ".tmp");
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        try (InputStream is = s3Object.getObjectContent();
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    public String getOriginalChecksum(String fileName) throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File("src/main/resources/checksums.properties");

        String value = "";
        if (propertiesFile.exists()) {
            try (InputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
            }
            value = properties.getProperty(fileName);
        }

        return value;
    }
}