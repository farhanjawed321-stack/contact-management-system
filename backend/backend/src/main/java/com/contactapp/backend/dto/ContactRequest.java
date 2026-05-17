package com.contactapp.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    private String firstName;
    private String lastName;
    private String title;
    private List<EmailDto> emailAddresses;
    private List<PhoneDto> phoneNumbers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailDto {
        private String email;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneDto {
        private String number;
        private String label;
    }
}