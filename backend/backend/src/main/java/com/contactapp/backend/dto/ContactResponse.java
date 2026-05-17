package com.contactapp.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private List<EmailDto> emailAddresses;
    private List<PhoneDto> phoneNumbers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailDto {
        private Long id;
        private String email;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PhoneDto {
        private Long id;
        private String number;
        private String label;
    }
}