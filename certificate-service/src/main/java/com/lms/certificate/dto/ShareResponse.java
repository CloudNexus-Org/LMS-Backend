package com.lms.certificate.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShareResponse {
    String shareUrl;
    String linkedInUrl;
    String twitterUrl;
}
