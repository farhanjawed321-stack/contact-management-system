package com.contactapp.backend.controller;

import com.contactapp.backend.dto.ContactRequest;
import com.contactapp.backend.dto.ContactResponse;
import com.contactapp.backend.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    private ContactResponse mockResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("farhan@test.com")
            .password("irrelevant")
            .authorities("ROLE_USER")
            .build();

        mockResponse = ContactResponse.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .title("Manager")
            .emailAddresses(List.of(
                ContactResponse.EmailDto.builder()
                    .id(1L)
                    .email("john@work.com")
                    .label("work")
                    .build()))
            .phoneNumbers(List.of(
                ContactResponse.PhoneDto.builder()
                    .id(1L)
                    .number("+923001234567")
                    .label("work")
                    .build()))
            .build();
    }

    @Test
    @DisplayName("✅ getAllContacts returns page response")
    void getAllContacts_Returns200() {

        Page<ContactResponse> page = new PageImpl<>(List.of(mockResponse));

        when(contactService.getAllContacts(
                anyString(),
                anyInt(),
                anyInt(),
                nullable(String.class)))
            .thenReturn(page);

        ResponseEntity<Page<ContactResponse>> response =
            contactController.getAllContacts(
                userDetails,
                0,
                10,
                null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getFirstName())
            .isEqualTo("John");
    }

    @Test
    @DisplayName("✅ getContact returns contact response")
    void getContact_Returns200() {

        when(contactService.getContact(anyString(), anyLong()))
            .thenReturn(mockResponse);

        ResponseEntity<ContactResponse> response =
            contactController.getContact(userDetails, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("✅ createContact returns created contact")
    void createContact_Returns201() {

        ContactRequest request = new ContactRequest(
            "John",
            "Doe",
            "Manager",
            List.of(new ContactRequest.EmailDto(
                "john@work.com",
                "work")),
            List.of(new ContactRequest.PhoneDto(
                "+923001234567",
                "work"))
        );

        when(contactService.createContact(
                anyString(),
                any(ContactRequest.class)))
            .thenReturn(mockResponse);

        ResponseEntity<ContactResponse> response =
            contactController.createContact(userDetails, request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("✅ updateContact returns updated contact")
    void updateContact_Returns200() {

        ContactRequest request = new ContactRequest(
            "John",
            "Updated",
            "Director",
            List.of(new ContactRequest.EmailDto(
                "john@new.com",
                "work")),
            List.of(new ContactRequest.PhoneDto(
                "+923009999999",
                "home"))
        );

        when(contactService.updateContact(
                anyString(),
                anyLong(),
                any(ContactRequest.class)))
            .thenReturn(mockResponse);

        ResponseEntity<ContactResponse> response =
            contactController.updateContact(
                userDetails,
                1L,
                request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("✅ deleteContact returns deletion message")
    void deleteContact_Returns200() {

        doNothing().when(contactService)
            .deleteContact(anyString(), anyLong());

        ResponseEntity<?> response =
            contactController.deleteContact(userDetails, 1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);

        assertThat(response.getBody())
            .isInstanceOf(java.util.Map.class);

        assertThat(((java.util.Map<?, ?>) response.getBody())
            .get("message"))
            .isEqualTo("Contact deleted successfully");
    }
}