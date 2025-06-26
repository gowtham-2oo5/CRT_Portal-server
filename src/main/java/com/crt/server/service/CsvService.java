package com.crt.server.service;

import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CsvService {
    /**
     * Parse a CSV file and return a list of CSVRecord objects
     * 
     * @param file    The CSV file to parse
     * @param headers The headers to use for the CSV file
     * @return List of CSVRecord objects
     * @throws IOException if there is an error reading the file
     */
    List<CSVRecord> parseCsv(MultipartFile file, String[] headers) throws IOException;

    /**
     * Parse a CSV file and map the records to a list of objects of type T
     * 
     * @param file    The CSV file to parse
     * @param headers The headers to use for the CSV file
     * @param mapper  The mapper to convert CSVRecord to type T
     * @return List of objects of type T
     * @throws IOException if there is an error reading the file
     */
    <T> List<T> parseCsv(MultipartFile file, String[] headers, RecordMapper<T> mapper) throws IOException;

    /**
     * Functional interface for mapping CSVRecord to a specific type
     * 
     * @param <T> The type to map to
     */
    @FunctionalInterface
    interface RecordMapper<T> {
        T map(CSVRecord record);
    }
}