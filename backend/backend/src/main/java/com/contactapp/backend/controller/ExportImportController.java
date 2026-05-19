package com.contactapp.backend.controller;

import com.contactapp.backend.service.ExportImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ExportImportController {

    private final ExportImportService exportImportService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportContacts(
            @AuthenticationPrincipal UserDetails userDetails)
            throws IOException {

        byte[] csv = exportImportService
            .exportContactsToCSV(userDetails.getUsername());

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=contacts.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @PostMapping(value = "/import",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        Map<String, Object> result = exportImportService
            .importContactsFromCSV(
                userDetails.getUsername(), file);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }
}
