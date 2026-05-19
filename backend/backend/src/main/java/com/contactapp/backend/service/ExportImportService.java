package com.contactapp.backend.service;

import com.contactapp.backend.entity.*;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExportImportService {

    private static final Logger log =
        LoggerFactory.getLogger(
            ExportImportService.class);

    private final ContactRepository
        contactRepository;
    private final UserRepository userRepository;

    // ─── EXPORT ──────────────────────────────

    public byte[] exportContactsToCSV(
            String email) throws IOException {

        log.info("Exporting contacts for: {}",
            email);

        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found",
                HttpStatus.NOT_FOUND));

        List<Contact> contacts =
            contactRepository
                .findByUserId(
                    user.getId(), 
                    org.springframework.data
                        .domain.Pageable.unpaged())
                .getContent();

        ByteArrayOutputStream out =
            new ByteArrayOutputStream();

        org.apache.commons.csv.CSVFormat exportFormat =
            CSVFormat.DEFAULT.builder()
                .setHeader(
                    "First Name",
                    "Last Name",
                    "Title",
                    "Email 1",
                    "Email 1 Label",
                    "Email 2",
                    "Email 2 Label",
                    "Phone 1",
                    "Phone 1 Label",
                    "Phone 2",
                    "Phone 2 Label"
                )
                .build();

        try (PrintWriter pw =
                new PrintWriter(out);
             CSVPrinter printer = new CSVPrinter(pw,
                 exportFormat)) {

            for (Contact contact : contacts) {
                List<EmailAddress> emails =
                    contact.getEmailAddresses();
                List<PhoneNumber> phones =
                    contact.getPhoneNumbers();

                printer.printRecord(
                    contact.getFirstName(),
                    contact.getLastName(),
                    contact.getTitle(),
                    getOrEmpty(emails, 0, "email"),
                    getOrEmpty(emails, 0, "label"),
                    getOrEmpty(emails, 1, "email"),
                    getOrEmpty(emails, 1, "label"),
                    getOrEmpty(phones, 0, "number"),
                    getOrEmpty(phones, 0, "label"),
                    getOrEmpty(phones, 1, "number"),
                    getOrEmpty(phones, 1, "label")
                );
            }
            printer.flush();
        }

        log.info("Exported {} contacts for: {}",
            contacts.size(), email);
        return out.toByteArray();
    }

    // ─── IMPORT ──────────────────────────────

    public Map<String, Object> importContactsFromCSV(
            String email,
            MultipartFile file)
            throws IOException {

        log.info("Importing contacts for: {}",
            email);

        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found",
                HttpStatus.NOT_FOUND));

        if (file.isEmpty()) {
            throw new AppException(
                "File is empty",
                HttpStatus.BAD_REQUEST);
        }

        // Validate file type
        String filename = file
            .getOriginalFilename();
        if (filename == null ||
            !filename.endsWith(".csv")) {
            throw new AppException(
                "Only CSV files are allowed",
                HttpStatus.BAD_REQUEST);
        }

        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        org.apache.commons.csv.CSVFormat importFormat =
            CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(
                file.getInputStream());
             CSVParser parser = new CSVParser(reader,
                 importFormat)) {

            for (CSVRecord record : parser) {
                try {
                    Contact contact =
                        Contact.builder()
                            .firstName(
                                getField(record,
                                "first name"))
                            .lastName(
                                getField(record,
                                "last name"))
                            .title(
                                getField(record,
                                "title"))
                            .user(user)
                            .emailAddresses(
                                new ArrayList<>())
                            .phoneNumbers(
                                new ArrayList<>())
                            .build();

                    // Validate first name
                    if (contact.getFirstName()
                            == null ||
                        contact.getFirstName()
                            .isEmpty()) {
                        skipped++;
                        errors.add("Row " +
                            parser.getCurrentLineNumber() +
                            ": First name required");
                        continue;
                    }

                    // Add emails
                    String email1 =
                        getField(record, "email 1");
                    String email1Label =
                        getField(record,
                            "email 1 label");
                    if (email1 != null &&
                        !email1.isEmpty()) {
                        contact.getEmailAddresses()
                            .add(EmailAddress
                                .builder()
                                .email(email1)
                                .label(email1Label
                                    != null ?
                                    email1Label :
                                    "work")
                                .contact(contact)
                                .build());
                    }

                    String email2 =
                        getField(record, "email 2");
                    String email2Label =
                        getField(record,
                            "email 2 label");
                    if (email2 != null &&
                        !email2.isEmpty()) {
                        contact.getEmailAddresses()
                            .add(EmailAddress
                                .builder()
                                .email(email2)
                                .label(email2Label
                                    != null ?
                                    email2Label :
                                    "work")
                                .contact(contact)
                                .build());
                    }

                    // Add phones
                    String phone1 =
                        getField(record, "phone 1");
                    String phone1Label =
                        getField(record,
                            "phone 1 label");
                    if (phone1 != null &&
                        !phone1.isEmpty()) {
                        contact.getPhoneNumbers()
                            .add(PhoneNumber
                                .builder()
                                .number(phone1)
                                .label(phone1Label
                                    != null ?
                                    phone1Label :
                                    "work")
                                .contact(contact)
                                .build());
                    }

                    String phone2 =
                        getField(record, "phone 2");
                    String phone2Label =
                        getField(record,
                            "phone 2 label");
                    if (phone2 != null &&
                        !phone2.isEmpty()) {
                        contact.getPhoneNumbers()
                            .add(PhoneNumber
                                .builder()
                                .number(phone2)
                                .label(phone2Label
                                    != null ?
                                    phone2Label :
                                    "work")
                                .contact(contact)
                                .build());
                    }

                    contactRepository.save(contact);
                    imported++;

                } catch (Exception e) {
                    skipped++;
                    errors.add("Row " +
                        parser
                            .getCurrentLineNumber()
                        + ": " + e.getMessage());
                }
            }
        }

        log.info(
            "Import complete - imported: {}, " +
            "skipped: {} for user: {}",
            imported, skipped, email);

        Map<String, Object> result =
            new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    // ─── HELPERS ─────────────────────────────

    private String getOrEmpty(
            List<?> list, int index,
            String field) {
        if (list == null ||
            index >= list.size()) return "";

        Object item = list.get(index);
        if (item instanceof EmailAddress) {
            return field.equals("email")
                ? ((EmailAddress) item).getEmail()
                : ((EmailAddress) item).getLabel();
        }
        if (item instanceof PhoneNumber) {
            return field.equals("number")
                ? ((PhoneNumber) item).getNumber()
                : ((PhoneNumber) item).getLabel();
        }
        return "";
    }

    private String getField(
            CSVRecord record, String name) {
        try {
            String value = record.get(name);
            return value != null &&
                !value.trim().isEmpty()
                ? value.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }
}