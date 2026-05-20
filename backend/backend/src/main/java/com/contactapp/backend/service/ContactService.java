package com.contactapp.backend.service;

import com.contactapp.backend.dto.*;
import com.contactapp.backend.entity.*;
import com.contactapp.backend.exception.AppException;
import com.contactapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Logger log = 
        LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    // ─── GET ALL CONTACTS (paginated) ───────────────────────────
    public Page<ContactResponse> getAllContacts(
            String email, int page, int size, String search) {

        User user = getUser(email);
        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("firstName").ascending());

        Page<Contact> contacts;

        if (search != null &&
            !search.trim().isEmpty()) {
            // Search by name, email OR phone!
            contacts = contactRepository
                .searchContacts(
                    user.getId(),
                    search.trim(),
                    pageable);
            log.debug(
                "Searching contacts for " +
                "user {} with query: {}",
                email, search);
        } else {
            contacts = contactRepository
                .findByUserId(
                    user.getId(), pageable);
        }

        return contacts.map(this::mapToResponse);
    }

    // ─── GET SINGLE CONTACT ──────────────────────────────────────
    public ContactResponse getContact(String email, Long contactId) {
        Contact contact = getContactAndVerifyOwner(email, contactId);
        log.debug("Fetched contact {} for user {}", contactId, email);
        return mapToResponse(contact);
    }

    // ─── CREATE CONTACT ──────────────────────────────────────────
    @Transactional
    public ContactResponse createContact(
            String email, ContactRequest request) {

        User user = getUser(email);
        log.info("Creating contact for user: {}", email);

        Contact contact = Contact.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .title(request.getTitle())
            .user(user)
            .build();

        // Map email addresses
        if (request.getEmailAddresses() != null) {
            List<EmailAddress> emails = request.getEmailAddresses()
                .stream()
                .map(e -> EmailAddress.builder()
                    .email(e.getEmail())
                    .label(e.getLabel())
                    .contact(contact)
                    .build())
                .collect(Collectors.toList());
            contact.setEmailAddresses(emails);
        }

        // Map phone numbers
        if (request.getPhoneNumbers() != null) {
            List<PhoneNumber> phones = request.getPhoneNumbers()
                .stream()
                .map(p -> PhoneNumber.builder()
                    .number(p.getNumber())
                    .label(p.getLabel())
                    .contact(contact)
                    .build())
                .collect(Collectors.toList());
            contact.setPhoneNumbers(phones);
        }

        Contact saved = contactRepository.save(contact);
        log.info("Contact created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    // ─── UPDATE CONTACT ──────────────────────────────────────────
    @Transactional
    public ContactResponse updateContact(
            String email, Long contactId, ContactRequest request) {

        Contact contact = getContactAndVerifyOwner(email, contactId);
        log.info("Updating contact {} for user {}", contactId, email);

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        // Clear and reset emails
        contact.getEmailAddresses().clear();
        if (request.getEmailAddresses() != null) {
            List<EmailAddress> emails = request.getEmailAddresses()
                .stream()
                .map(e -> EmailAddress.builder()
                    .email(e.getEmail())
                    .label(e.getLabel())
                    .contact(contact)
                    .build())
                .collect(Collectors.toList());
            contact.getEmailAddresses().addAll(emails);
        }

        // Clear and reset phones
        contact.getPhoneNumbers().clear();
        if (request.getPhoneNumbers() != null) {
            List<PhoneNumber> phones = request.getPhoneNumbers()
                .stream()
                .map(p -> PhoneNumber.builder()
                    .number(p.getNumber())
                    .label(p.getLabel())
                    .contact(contact)
                    .build())
                .collect(Collectors.toList());
            contact.getPhoneNumbers().addAll(phones);
        }

        Contact updated = contactRepository.save(contact);
        log.info("Contact {} updated successfully", contactId);
        return mapToResponse(updated);
    }

    // ─── DELETE CONTACT ──────────────────────────────────────────
    @Transactional
    public void deleteContact(String email, Long contactId) {
        Contact contact = getContactAndVerifyOwner(email, contactId);
        contactRepository.delete(contact);
        log.info("Contact {} deleted by user {}", contactId, email);
    }

    // ─── HELPER: Get user or throw ───────────────────────────────
    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(
                "User not found", HttpStatus.NOT_FOUND));
    }

    // ─── HELPER: Get contact & verify it belongs to user ─────────
    private Contact getContactAndVerifyOwner(
            String email, Long contactId) {

        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new AppException(
                "Contact not found", HttpStatus.NOT_FOUND));

        if (!contact.getUser().getEmail().equals(email)) {
            log.warn("User {} tried to access contact {} they don't own",
                email, contactId);
            throw new AppException(
                "Access denied", HttpStatus.FORBIDDEN);
        }

        return contact;
    }

    // ─── HELPER: Map entity to response DTO ──────────────────────
    private ContactResponse mapToResponse(Contact contact) {
        List<ContactResponse.EmailDto> emails = null;
        List<ContactResponse.PhoneDto> phones = null;

        if (contact.getEmailAddresses() != null) {
            emails = contact.getEmailAddresses().stream()
                .map(e -> ContactResponse.EmailDto.builder()
                    .id(e.getId())
                    .email(e.getEmail())
                    .label(e.getLabel())
                    .build())
                .collect(Collectors.toList());
        }

        if (contact.getPhoneNumbers() != null) {
            phones = contact.getPhoneNumbers().stream()
                .map(p -> ContactResponse.PhoneDto.builder()
                    .id(p.getId())
                    .number(p.getNumber())
                    .label(p.getLabel())
                    .build())
                .collect(Collectors.toList());
        }

        return ContactResponse.builder()
            .id(contact.getId())
            .firstName(contact.getFirstName())
            .lastName(contact.getLastName())
            .title(contact.getTitle())
            .emailAddresses(emails)
            .phoneNumbers(phones)
            .build();
    }
}