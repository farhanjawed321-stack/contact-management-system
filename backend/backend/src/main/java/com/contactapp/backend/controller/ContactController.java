package com.contactapp.backend.controller;

import com.contactapp.backend.dto.*;
import com.contactapp.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // GET all contacts (with pagination + search)
    // GET /api/contacts?page=0&size=10&search=john
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<ContactResponse> contacts = contactService
            .getAllContacts(
                userDetails.getUsername(), page, size, search);
        return ResponseEntity.ok(contacts);
    }

    // GET single contact
    // GET /api/contacts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        ContactResponse contact = contactService
            .getContact(userDetails.getUsername(), id);
        return ResponseEntity.ok(contact);
    }

    // CREATE new contact
    // POST /api/contacts
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ContactRequest request) {

        ContactResponse contact = contactService
            .createContact(userDetails.getUsername(), request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(contact);
    }

    // UPDATE contact
    // PUT /api/contacts/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody ContactRequest request) {

        ContactResponse contact = contactService
            .updateContact(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(contact);
    }

    // DELETE contact
    // DELETE /api/contacts/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        contactService.deleteContact(userDetails.getUsername(), id);
        return ResponseEntity.ok(
            java.util.Map.of("message", "Contact deleted successfully"));
    }
}