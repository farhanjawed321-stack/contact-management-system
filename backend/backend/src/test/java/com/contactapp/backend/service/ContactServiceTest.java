package com.contactapp.backend.service;

import com.contactapp.backend.dto.*;
import com.contactapp.backend.entity.*;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    // Reusable test data
    private User mockUser;
    private Contact mockContact;

    // @BeforeEach runs before every single test
    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .firstName("Farhan")
            .lastName("Jawed")
            .build();

        mockContact = Contact.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .title("Manager")
            .user(mockUser)
            .emailAddresses(new ArrayList<>())
            .phoneNumbers(new ArrayList<>())
            .build();
    }

    // ─── GET ALL CONTACTS TESTS ───────────────────────────────────

    @Test
    @DisplayName("✅ Get All Contacts - Returns paginated list")
    void getAllContacts_ShouldReturnPagedContacts() {
        // ARRANGE
        List<Contact> contacts = List.of(mockContact);
        Page<Contact> mockPage = new PageImpl<>(contacts);

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(contactRepository.findByUserId(
                anyLong(), any(Pageable.class)))
            .thenReturn(mockPage);

        // ACT
        Page<ContactResponse> result =
            contactService.getAllContacts(
                "farhan@test.com", 0, 10, null);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John",
            result.getContent().get(0).getFirstName());
    }

    @Test
    @DisplayName("✅ Search Contacts - Returns filtered list")
    void getAllContacts_WithSearch_ShouldReturnFiltered() {
        // ARRANGE
        List<Contact> contacts = List.of(mockContact);
        Page<Contact> mockPage = new PageImpl<>(contacts);

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(contactRepository.searchContacts(
                anyLong(), anyString(), any(Pageable.class)))
            .thenReturn(mockPage);

        // ACT
        Page<ContactResponse> result =
            contactService.getAllContacts(
                "farhan@test.com", 0, 10, "John");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contactRepository, times(1))
            .searchContacts(anyLong(), anyString(),
                any(Pageable.class));
    }

    // ─── GET SINGLE CONTACT TESTS ─────────────────────────────────

    @Test
    @DisplayName("✅ Get Contact - Success")
    void getContact_ValidId_ShouldReturnContact() {
        // ARRANGE
        when(contactRepository.findById(anyLong()))
            .thenReturn(Optional.of(mockContact));

        // ACT
        ContactResponse result =
            contactService.getContact("farhan@test.com", 1L);

        // ASSERT
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Manager", result.getTitle());
    }

    @Test
    @DisplayName("❌ Get Contact - Not found")
    void getContact_InvalidId_ShouldThrowException() {
        // ARRANGE
        when(contactRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> contactService.getContact(
                "farhan@test.com", 99L));

        assertEquals("Contact not found",
            exception.getMessage());
    }

    @Test
    @DisplayName("❌ Get Contact - Access denied")
    void getContact_WrongUser_ShouldThrowException() {
        // ARRANGE - contact belongs to different user!
        User otherUser = User.builder()
            .id(2L)
            .email("other@test.com")
            .build();

        Contact otherContact = Contact.builder()
            .id(1L)
            .firstName("John")
            .user(otherUser) // belongs to other user
            .emailAddresses(new ArrayList<>())
            .phoneNumbers(new ArrayList<>())
            .build();

        when(contactRepository.findById(anyLong()))
            .thenReturn(Optional.of(otherContact));

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> contactService.getContact(
                "farhan@test.com", 1L));

        assertEquals("Access denied", exception.getMessage());
    }

    // ─── CREATE CONTACT TESTS ─────────────────────────────────────

    @Test
    @DisplayName("✅ Create Contact - Success")
    void createContact_ValidRequest_ShouldReturnContact() {
        // ARRANGE
        ContactRequest request = new ContactRequest(
            "Jane", "Smith", "Director",
            List.of(new ContactRequest.EmailDto(
                "jane@work.com", "work")),
            List.of(new ContactRequest.PhoneDto(
                "+923001234567", "work")));

        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));
        when(contactRepository.save(any(Contact.class)))
            .thenReturn(mockContact);

        // ACT
        ContactResponse result =
            contactService.createContact(
                "farhan@test.com", request);

        // ASSERT
        assertNotNull(result);
        verify(contactRepository, times(1))
            .save(any(Contact.class));
    }

    // ─── DELETE CONTACT TESTS ─────────────────────────────────────

    @Test
    @DisplayName("✅ Delete Contact - Success")
    void deleteContact_ValidId_ShouldDelete() {
        // ARRANGE
        when(contactRepository.findById(anyLong()))
            .thenReturn(Optional.of(mockContact));

        // ACT
        assertDoesNotThrow(() ->
            contactService.deleteContact(
                "farhan@test.com", 1L));

        // Verify delete was called
        verify(contactRepository, times(1))
            .delete(any(Contact.class));
    }

    @Test
    @DisplayName("❌ Delete Contact - Not found")
    void deleteContact_InvalidId_ShouldThrowException() {
        // ARRANGE
        when(contactRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        // ASSERT
        AppException exception = assertThrows(
            AppException.class,
            () -> contactService.deleteContact(
                "farhan@test.com", 99L));

        assertEquals("Contact not found",
            exception.getMessage());
        verify(contactRepository, never())
            .delete(any(Contact.class));
    }
}