package com.crt.server.service.impl;

import com.crt.server.service.CsvService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvServiceImpl implements CsvService {

    @Override
    public List<CSVRecord> parseCsv(MultipartFile file, String[] headers) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(headers)
                    .setSkipHeaderRecord(true)
                    .get();

            try (CSVParser csvParser = csvFormat.parse(reader)) {
                return csvParser.getRecords();
            }
        } catch (IOException e) {
            log.error("Error parsing CSV file: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public <T> List<T> parseCsv(MultipartFile file, String[] headers, RecordMapper<T> mapper) throws IOException {
        List<T> results = new ArrayList<>();
        try {
            for (CSVRecord record : parseCsv(file, headers)) {
                results.add(mapper.map(record));
            }
        } catch (Exception e) {
            log.error("Error mapping CSV records: {}", e.getMessage());
            throw new IOException("Error mapping CSV records", e);
        }
        return results;
    }
    @Override
    public List<CSVRecord> parseCsvWithoutHeaders(MultipartFile file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream())) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .get();

            CSVParser parser = format.parse(reader);
            return parser.getRecords();
        } catch (Exception e) {
            log.error("Error parsing CSV file without headers: {}", e.getMessage());
            throw new IOException("Error parsing CSV file", e);
        }
    }
}