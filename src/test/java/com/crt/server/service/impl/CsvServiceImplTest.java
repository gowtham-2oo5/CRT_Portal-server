package com.crt.server.service.impl;

import com.crt.server.service.CsvService;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CsvServiceImplTest {

    @InjectMocks
    private CsvServiceImpl csvService;

    private MockMultipartFile validCsvFile;
    private MockMultipartFile invalidCsvFile;
    private String[] headers;

    @BeforeEach
    void setUp() {
        // Create a valid CSV file content
        String validContent = "name,email,phone\nJohn Doe,john@example.com,1234567890\nJane Doe,jane@example.com,0987654321";
        validCsvFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                validContent.getBytes());

        // Create an invalid CSV file content
        String invalidContent = "invalid,csv,content";
        invalidCsvFile = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                invalidContent.getBytes());

        headers = new String[] { "name", "email", "phone" };
    }

    @Test
    void parseCsv_WithValidFile_ShouldReturnRecords() throws IOException {
        // Act
        List<CSVRecord> records = csvService.parseCsv(validCsvFile, headers);

        // Assert
        assertNotNull(records);
        assertEquals(2, records.size());

        // Verify first record
        CSVRecord firstRecord = records.get(0);
        assertEquals("John Doe", firstRecord.get("name"));
        assertEquals("john@example.com", firstRecord.get("email"));
        assertEquals("1234567890", firstRecord.get("phone"));

        // Verify second record
        CSVRecord secondRecord = records.get(1);
        assertEquals("Jane Doe", secondRecord.get("name"));
        assertEquals("jane@example.com", secondRecord.get("email"));
        assertEquals("0987654321", secondRecord.get("phone"));
    }

    @Test
    void parseCsv_WithValidFileAndMapper_ShouldReturnMappedObjects() throws IOException {
        // Arrange
        CsvService.RecordMapper<TestDTO> mapper = record -> new TestDTO(
                record.get("name"),
                record.get("email"),
                record.get("phone"));

        // Act
        List<TestDTO> results = csvService.parseCsv(validCsvFile, headers, mapper);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());

        // Verify first record
        TestDTO firstDTO = results.get(0);
        assertEquals("John Doe", firstDTO.name);
        assertEquals("john@example.com", firstDTO.email);
        assertEquals("1234567890", firstDTO.phone);

        // Verify second record
        TestDTO secondDTO = results.get(1);
        assertEquals("Jane Doe", secondDTO.name);
        assertEquals("jane@example.com", secondDTO.email);
        assertEquals("0987654321", secondDTO.phone);
    }

    @Test
    void parseCsv_WithInvalidFile_ShouldThrowIOException() {
        // Act & Assert
        assertThrows(IOException.class, () -> csvService.parseCsv(invalidCsvFile, headers));
    }

    @Test
    void parseCsv_WithInvalidMapper_ShouldThrowIOException() {
        // Arrange
        CsvService.RecordMapper<TestDTO> invalidMapper = record -> {
            throw new RuntimeException("Invalid mapping");
        };

        // Act & Assert
        assertThrows(IOException.class, () -> csvService.parseCsv(validCsvFile, headers, invalidMapper));
    }

    // Test DTO class
    private static class TestDTO {
        private final String name;
        private final String email;
        private final String phone;

        public TestDTO(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }
}