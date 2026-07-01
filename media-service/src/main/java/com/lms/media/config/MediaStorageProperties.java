package com.lms.media.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "media.storage")
public class MediaStorageProperties {
    private String root = "./uploads";
    private String baseUrl = "http://localhost:8095/api/media/files";
}
