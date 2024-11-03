package com.heraj.s3_client_side_encryption.controller;

import com.heraj.s3_client_side_encryption.util.AesEncryption;
import com.heraj.s3_client_side_encryption.service.FileUploadService;
import com.heraj.s3_client_side_encryption.service.KeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private FileUploadService s3UploadService;

    @Autowired
    private KeyService keyService;

    @Autowired
    private AesEncryption aesEncryption;

    @Value("spring.servlet.multipart.location")
    private String multipartLocation;

    @GetMapping("/")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile mfile, Model model) throws Exception {

        logger.info("uploadFile - start");

        Path tempDir = Paths.get(multipartLocation);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        try {
            s3UploadService.uploadFile(mfile, keyService.getSecretKey());
            model.addAttribute("message", "File uploaded successfully");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }
        return "upload";
    }
}
