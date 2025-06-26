package com.crt.server.service.impl;

import com.crt.server.dto.TrainerDTO;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.CRT_Trainer;
import com.crt.server.repository.TrainerRepository;
import com.crt.server.service.CsvService;
import com.crt.server.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final CsvService csvService;

    @Override
    public TrainerDTO createTrainer(TrainerDTO trainerDTO) {
        if (existsByEmail(trainerDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (existsBySn(trainerDTO.getSn())) {
            throw new RuntimeException("Short name already exists");
        }

        CRT_Trainer trainer = CRT_Trainer.builder()
                .name(trainerDTO.getName())
                .email(trainerDTO.getEmail())
                .sn(trainerDTO.getSn())
                .build();

        CRT_Trainer savedTrainer = trainerRepository.save(trainer);
        return convertToDTO(savedTrainer);
    }

    @Override
    public TrainerDTO getTrainerById(UUID id) {
        CRT_Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        return convertToDTO(trainer);
    }

    @Override
    public TrainerDTO getTrainerByEmail(String email) {
        try {
            CRT_Trainer trainer = trainerRepository.findByEmail(email);
            return convertToDTO(trainer);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Trainer not found with email: " + email);
        }
    }

    @Override
    public TrainerDTO getTrainerBySn(String sn) {
        try {
            CRT_Trainer trainer = trainerRepository.findBySn(sn);
            return convertToDTO(trainer);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Trainer not found with short name: " + sn);
        }
    }

    @Override
    public List<TrainerDTO> getAllTrainers() {
        return trainerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TrainerDTO updateTrainer(UUID id, TrainerDTO trainerDTO) {
        CRT_Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        trainer.setName(trainerDTO.getName());
        trainer.setEmail(trainerDTO.getEmail());
        trainer.setSn(trainerDTO.getSn());

        CRT_Trainer updatedTrainer = trainerRepository.save(trainer);
        return convertToDTO(updatedTrainer);
    }

    @Override
    public void deleteTrainer(UUID id) {
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer not found");
        }
        trainerRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return trainerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsBySn(String sn) {
        return trainerRepository.existsBySn(sn);
    }

    @Override
    public List<TrainerDTO> bulkCreateTrainers(MultipartFile file) throws Exception {
        String[] headers = {"name", "email", "sn"};

        try {
            List<CRT_Trainer> trainers = csvService.parseCsv(file, headers, record -> {
                // Validate required fields
                validateRequiredFields(record);

                // Create and return Trainer entity
                return CRT_Trainer.builder()
                        .name(record.get("name"))
                        .email(record.get("email"))
                        .sn(record.get("sn"))
                        .build();
            });

            // Save all trainers in a single transaction
            List<CRT_Trainer> savedTrainers = trainerRepository.saveAll(trainers);
            return savedTrainers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in bulk trainer creation: {}", e.getMessage());
            throw new Exception("Error processing trainer data: " + e.getMessage());
        }
    }

    private void validateRequiredFields(org.apache.commons.csv.CSVRecord record) {
        String[] requiredFields = {"name", "email", "sn"};
        for (String field : requiredFields) {
            if (!record.isSet(field) || record.get(field).trim().isEmpty()) {
                throw new IllegalArgumentException("Required field '" + field + "' is missing or empty");
            }
        }

        // Validate email format
        String email = record.get("email").trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email format for: " + email);
        }
    }

    private TrainerDTO convertToDTO(CRT_Trainer trainer) {
        return TrainerDTO.builder()
                .id(trainer.getId())
                .name(trainer.getName())
                .email(trainer.getEmail())
                .sn(trainer.getSn())
                .build();
    }
}