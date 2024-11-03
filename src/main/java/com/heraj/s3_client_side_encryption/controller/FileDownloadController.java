package com.heraj.s3_client_side_encryption.controller;

import com.heraj.s3_client_side_encryption.util.AesEncryption;
import com.heraj.s3_client_side_encryption.service.FileDownloadService;
import com.heraj.s3_client_side_encryption.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.SecretKey;
import java.io.File;

@Controller
public class FileDownloadController {

    @Autowired
    private FileDownloadService fileService;

    @Autowired
    private KeyService keyService;

    @Autowired
    private AesEncryption aesEncryption;

    @GetMapping("/download")
    public String downloadFile(@RequestParam("fileName") String fileName, Model model) {
        try {
            File downloadedFile = fileService.downloadFile(fileName);

            SecretKey secretKey = keyService.getSecretKey();

            File decryptedFile = File.createTempFile(fileName.substring(0, fileName.lastIndexOf('.')), fileName.substring(fileName.lastIndexOf('.')));
            aesEncryption.decryptFile(downloadedFile, decryptedFile, secretKey);

            String originalChecksum = fileService.getOriginalChecksum(fileName); // Retrieve the stored checksum
            String decryptedChecksum = AesEncryption.generateChecksum(decryptedFile);

            System.out.println(decryptedChecksum);
            if (decryptedChecksum.equals(originalChecksum)) {
                model.addAttribute("message", "File downloaded and verified successfully.");
            } else {
                System.out.println(decryptedChecksum);
                model.addAttribute("message", "Integrity check failed!");
            }

            downloadedFile.delete();
            decryptedFile.delete();
        } catch (Exception e) {
            model.addAttribute("message", "Failed to download or verify file: " + e.getMessage());
        }
        return "upload";
    }
}
