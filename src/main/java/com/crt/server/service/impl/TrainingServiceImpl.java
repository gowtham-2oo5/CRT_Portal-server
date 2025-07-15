package com.crt.server.service.impl;

import com.crt.server.dto.TrainingDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.Training;
import com.crt.server.repository.TrainingRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepo;
    private final CsvService csvService;

    @Override
    public TrainingDTO createTraining(TrainingDTO trainingDTO) {
        if (existsBySn(trainingDTO.getSn())) {
            throw new RuntimeException("Short name already exists");
        }

        Training training = Training.builder()
                .name(trainingDTO.getName())
                .sn(trainingDTO.getSn())
                .build();

        Training savedTraining = trainingRepo.save(training);
        return convertToDTO(savedTraining);
    }

    @Override
    public TrainingDTO getTrainingById(UUID id) {
        Training training = trainingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));
        return convertToDTO(training);
    }


    @Override
    public Training getTrainingBySn(String sn) {
        try {
            return trainingRepo.findBySn(sn);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Training not found with short name: " + sn);
        }
    }

    @Override
    public Training getTrainingByName(String name) {
        return trainingRepo.findByName(name);
    }

    @Override
    public List<TrainingDTO> getAllTrainings() {
        return trainingRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TrainingDTO updateTraining(UUID id, TrainingDTO trainingDTO) {
        Training training = trainingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));

        training.setName(trainingDTO.getName());
        training.setSn(trainingDTO.getSn());

        Training updatedTraining = trainingRepo.save(training);
        return convertToDTO(updatedTraining);
    }

    @Override
    public void deleteTraining(UUID id) {
        if (!trainingRepo.existsById(id)) {
            throw new ResourceNotFoundException("Training not found");
        }
        trainingRepo.deleteById(id);
    }


    @Override
    public boolean existsBySn(String sn) {
        return trainingRepo.existsBySn(sn);
    }

    @Override
    public boolean existsByName(String name) {
        return trainingRepo.existsByName(name);
    }

    @Override
    public List<TrainingDTO> bulkCreateTrainings(MultipartFile file) throws Exception {
        String[] headers = {"NAME", "SN"};

        try {
            List<Training> trainings = csvService.parseCsv(file, headers, record -> {
                validateRequiredFields(record);

                return Training.builder()
                        .name(record.get("NAME"))
                        .sn((record.get("SN") == null) ? record.get("NAME").trim().substring(0, 2) : record.get("SN"))
                        .sections(new HashSet<>())
                        .build();
            });

            List<Training> savedTrainings = trainingRepo.saveAll(trainings);
            return savedTrainings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk Training creation: {}", e.getMessage());
            throw new Exception("Error processing Training data: " + e.getMessage());
        }
    }

    private void validateRequiredFields(org.apache.commons.csv.CSVRecord record) {
        String[] requiredFields = {"NAME", "SN"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new IllegalArgumentException("Required field '" + field + "' is missing or empty");
            }
        }
    }

    private TrainingDTO convertToDTO(Training training) {
        return TrainingDTO.builder()
                .id(training.getId())
                .name(training.getName())
                .sn(training.getSn())
                .sections(training.getSections().size())
                .build();
    }
}