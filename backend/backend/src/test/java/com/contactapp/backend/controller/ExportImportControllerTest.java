package com.contactapp.backend.controller;


import com.contactapp.backend.service.ExportImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportImportControllerTest {

    @Mock
    private ExportImportService exportImportService;

    @InjectMocks
    private ExportImportController exportImportController;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
    }

    @Test
    void testExportContacts() throws IOException {
        byte[] dummyData = "test data".getBytes();
        when(exportImportService.exportContactsToCSV("testuser@example.com")).thenReturn(dummyData);

        ResponseEntity<byte[]> response = exportImportController.exportContacts(userDetails);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(dummyData, response.getBody());
    }

    @Test
    void testImportContacts() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "contacts.csv", "text/csv", "name,email\nJohn,john@example.com".getBytes());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("imported", 1);
        resultMap.put("skipped", 0);
        resultMap.put("errors", new java.util.ArrayList<>());
        when(exportImportService.importContactsFromCSV(eq("testuser@example.com"), any(MultipartFile.class))).thenReturn(resultMap);

        ResponseEntity<Map<String, Object>> response = exportImportController.importContacts(userDetails, file);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultMap, response.getBody());
    }
}
