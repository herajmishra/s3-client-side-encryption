package com.heraj.s3_client_side_encryption.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HomeDirectoryConfig {

    @Value("${app.home.directory}")
    private String homeDirectory;

    public String getHomeDirectory() {
        return homeDirectory;
    }
}