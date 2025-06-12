package com.crt.server.service;

import com.crt.server.dto.TrainerDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TrainerService {
    TrainerDTO createTrainer(TrainerDTO trainerDTO);

    TrainerDTO getTrainerById(UUID id);

    TrainerDTO getTrainerByEmail(String email);

    TrainerDTO getTrainerBySn(String sn);

    List<TrainerDTO> getAllTrainers();

    TrainerDTO updateTrainer(UUID id, TrainerDTO trainerDTO);

    void deleteTrainer(UUID id);

    boolean existsByEmail(String email);

    boolean existsBySn(String sn);

    /**
     * Bulk create trainers from a CSV file
     * 
     * @param file The CSV file containing trainer data
     * @return List of created trainer DTOs
     * @throws Exception if there is an error processing the file or creating
     *                   trainers
     */
    List<TrainerDTO> bulkCreateTrainers(MultipartFile file) throws Exception;
}