package com.contactapp.backend.service;

import com.contactapp.backend.entity.*;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportImportServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExportImportService exportImportService;

    private User mockUser;
    private Contact mockContact;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .email("farhan@test.com")
            .firstName("Farhan")
            .lastName("Jawed")
            .build();

        // Create email and phone lists
        EmailAddress email = EmailAddress.builder()
            .id(1L)
            .email("john@work.com")
            .label("work")
            .build();

        PhoneNumber phone = PhoneNumber.builder()
            .id(1L)
            .number("+923001234567")
            .label("work")
            .build();

        mockContact = Contact.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .title("Manager")
            .user(mockUser)
            .emailAddresses(
                new ArrayList<>(List.of(email)))
            .phoneNumbers(
                new ArrayList<>(List.of(phone)))
            .build();

        // Set contact reference
        email.setContact(mockContact);
        phone.setContact(mockContact);
    }

    // ─── EXPORT TESTS ─────────────────────────

    @Test
    @DisplayName("✅ Export - Success with contacts")
    void exportContacts_Success()
            throws IOException {

        // ARRANGE
        Page<Contact> mockPage =
            new PageImpl<>(
                List.of(mockContact));

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository.findByUserId(
                anyLong(), any(Pageable.class)))
            .thenReturn(mockPage);

        // ACT
        byte[] result = exportImportService
            .exportContactsToCSV(
                "farhan@test.com");

        // ASSERT
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify CSV content
        String csvContent = new String(result);
        assertTrue(csvContent
            .contains("First Name"));
        assertTrue(csvContent
            .contains("John"));
        assertTrue(csvContent
            .contains("john@work.com"));
        assertTrue(csvContent
            .contains("+923001234567"));

        verify(contactRepository, times(1))
            .findByUserId(
                anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("✅ Export - Empty contacts list")
    void exportContacts_EmptyList_ReturnsCSVWithHeaderOnly()
            throws IOException {

        // ARRANGE - no contacts
        Page<Contact> emptyPage =
            new PageImpl<>(new ArrayList<>());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository.findByUserId(
                anyLong(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // ACT
        byte[] result = exportImportService
            .exportContactsToCSV(
                "farhan@test.com");

        // ASSERT
        assertNotNull(result);
        String csvContent = new String(result);

        // Should have headers but no data rows
        assertTrue(csvContent
            .contains("First Name"));
        assertFalse(csvContent
            .contains("John"));
    }

    @Test
    @DisplayName("❌ Export - User not found")
    void exportContacts_UserNotFound_ThrowsException() {

        // ARRANGE
        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(Optional.empty());

        // ASSERT
        AppException ex = assertThrows(
            AppException.class,
            () -> exportImportService
                .exportContactsToCSV(
                    "notfound@test.com"));

        assertEquals("User not found",
            ex.getMessage());

        // Verify repository never called
        verify(contactRepository, never())
            .findByUserId(
                anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("✅ Export - Contact with no emails or phones")
    void exportContacts_ContactWithNoEmailsOrPhones()
            throws IOException {

        // ARRANGE - contact with empty lists
        Contact emptyContact = Contact.builder()
            .id(2L)
            .firstName("Jane")
            .lastName("Smith")
            .title("Developer")
            .user(mockUser)
            .emailAddresses(new ArrayList<>())
            .phoneNumbers(new ArrayList<>())
            .build();

        Page<Contact> mockPage =
            new PageImpl<>(
                List.of(emptyContact));

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository.findByUserId(
                anyLong(), any(Pageable.class)))
            .thenReturn(mockPage);

        // ACT
        byte[] result = exportImportService
            .exportContactsToCSV(
                "farhan@test.com");

        // ASSERT
        assertNotNull(result);
        String csv = new String(result);
        assertTrue(csv.contains("Jane"));
        assertTrue(csv.contains("Smith"));
    }

    // ─── IMPORT TESTS ─────────────────────────

    @Test
    @DisplayName("✅ Import - Success")
    void importContacts_Success()
            throws IOException {

        // ARRANGE - create a valid CSV file
        String csvContent =
            "First Name,Last Name,Title," +
            "Email 1,Email 1 Label," +
            "Email 2,Email 2 Label," +
            "Phone 1,Phone 1 Label," +
            "Phone 2,Phone 2 Label\n" +
            "John,Doe,Manager," +
            "john@work.com,work,,," +
            "+923001234567,work,,\n" +
            "Jane,Smith,Developer," +
            "jane@email.com,personal,,," +
            "+923009999999,home,,\n";

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csvContent.getBytes());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository
                .save(any(Contact.class)))
            .thenReturn(mockContact);

        // ACT
        Map<String, Object> result =
            exportImportService
                .importContactsFromCSV(
                    "farhan@test.com", file);

        // ASSERT
        assertNotNull(result);
        assertEquals(2,
            result.get("imported"));
        assertEquals(0,
            result.get("skipped"));

        // Verify save called twice
        verify(contactRepository, times(2))
            .save(any(Contact.class));
    }

    @Test
    @DisplayName("❌ Import - Empty file")
    void importContacts_EmptyFile_ThrowsException() {

        // ARRANGE - empty file
        MockMultipartFile emptyFile =
            new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                new byte[0]);

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));

        // ASSERT
        AppException ex = assertThrows(
            AppException.class,
            () -> exportImportService
                .importContactsFromCSV(
                    "farhan@test.com",
                    emptyFile));

        assertEquals("File is empty",
            ex.getMessage());
    }

    @Test
    @DisplayName("❌ Import - Wrong file type")
    void importContacts_WrongFileType_ThrowsException() {

        // ARRANGE - txt file not csv!
        MockMultipartFile txtFile =
            new MockMultipartFile(
                "file",
                "contacts.txt",
                "text/plain",
                "some content".getBytes());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));

        // ASSERT
        AppException ex = assertThrows(
            AppException.class,
            () -> exportImportService
                .importContactsFromCSV(
                    "farhan@test.com",
                    txtFile));

        assertEquals(
            "Only CSV files are allowed",
            ex.getMessage());
    }

    @Test
    @DisplayName("❌ Import - User not found")
    void importContacts_UserNotFound_ThrowsException() {

        // ARRANGE
        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                "content".getBytes());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(Optional.empty());

        // ASSERT
        AppException ex = assertThrows(
            AppException.class,
            () -> exportImportService
                .importContactsFromCSV(
                    "notfound@test.com",
                    file));

        assertEquals("User not found",
            ex.getMessage());
    }

    @Test
    @DisplayName("✅ Import - Skip rows with missing first name")
    void importContacts_SkipRowsMissingFirstName()
            throws IOException {

        // ARRANGE - one valid, one invalid row
        String csvContent =
            "First Name,Last Name,Title," +
            "Email 1,Email 1 Label," +
            "Email 2,Email 2 Label," +
            "Phone 1,Phone 1 Label," +
            "Phone 2,Phone 2 Label\n" +
            "John,Doe,Manager," +
            "john@work.com,work,,,," +
            "+923001234567,work,\n" +
            // Missing first name!
            ",Smith,Developer," +
            "jane@email.com,personal,,,," +
            "+923009999999,home,\n";

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csvContent.getBytes());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository
                .save(any(Contact.class)))
            .thenReturn(mockContact);

        // ACT
        Map<String, Object> result =
            exportImportService
                .importContactsFromCSV(
                    "farhan@test.com", file);

        // ASSERT
        assertEquals(1,
            result.get("imported"));
        assertEquals(1,
            result.get("skipped"));

        // Only saved once (valid row)
        verify(contactRepository, times(1))
            .save(any(Contact.class));
    }

    @Test
    @DisplayName("✅ Import - Contact with multiple emails and phones")
    void importContacts_MultipleEmailsAndPhones()
            throws IOException {

        // ARRANGE
        String csvContent =
            "First Name,Last Name,Title," +
            "Email 1,Email 1 Label," +
            "Email 2,Email 2 Label," +
            "Phone 1,Phone 1 Label," +
            "Phone 2,Phone 2 Label\n" +
            "John,Doe,Manager," +
            "john@work.com,work," +
            "john@gmail.com,personal," +
            "+923001234567,work," +
            "+923007654321,home\n";

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csvContent.getBytes());

        when(userRepository.findByEmail(
                anyString()))
            .thenReturn(
                Optional.of(mockUser));
        when(contactRepository
                .save(any(Contact.class)))
            .thenReturn(mockContact);

        // ACT
        Map<String, Object> result =
            exportImportService
                .importContactsFromCSV(
                    "farhan@test.com", file);

        // ASSERT
        assertEquals(1,
            result.get("imported"));
        assertEquals(0,
            result.get("skipped"));
        verify(contactRepository, times(1))
            .save(any(Contact.class));
    }
}