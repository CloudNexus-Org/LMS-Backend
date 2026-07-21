package com.lms.auth.dto;

import lombok.Data;

@Data
public class SyncProfileNameRequest {
    private String email;
    private String fullName;
}
