package com.crt.server.service;

import com.crt.server.dto.TrainingDTO;
import com.crt.server.model.Training;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TrainingService {
    TrainingDTO createTraining(TrainingDTO TrainingDTO);

    TrainingDTO getTrainingById(UUID id);


    Training getTrainingBySn(String sn);

    Training getTrainingByName(String name);

    List<TrainingDTO> getAllTrainings();

    TrainingDTO updateTraining(UUID id, TrainingDTO TrainingDTO);

    void deleteTraining(UUID id);

    boolean existsBySn(String sn);

    boolean existsByName(String name);

    /**
     * Bulk create Trainings from a CSV file
     * 
     * @param file The CSV file containing Training data
     * @return List of created Training DTOs
     * @throws Exception if there is an error processing the file or creating
     *                   Trainings
     */
    List<TrainingDTO> bulkCreateTrainings(MultipartFile file) throws Exception;
}