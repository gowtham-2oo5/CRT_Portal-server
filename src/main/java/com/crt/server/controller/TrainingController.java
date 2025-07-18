package com.crt.server.controller;

import com.crt.server.dto.TrainingDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @PostMapping
    public ResponseEntity<?> createTraining(@RequestBody TrainingDTO trainingDTO) {
        try {
            log.info("Creating training: {}", trainingDTO);
            TrainingDTO response = trainingService.createTraining(trainingDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.CONFLICT.value())
                    .error(HttpStatus.CONFLICT.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/Trainings")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingDTO> getTrainingById(@PathVariable UUID id) {
        return ResponseEntity.ok(trainingService.getTrainingById(id));
    }


    @GetMapping("/sn/{sn}")
    public ResponseEntity<?> getTrainingBySn(@PathVariable String sn) {
        return ResponseEntity.ok(trainingService.getTrainingBySn(sn));
    }

    @GetMapping
    public ResponseEntity<List<TrainingDTO>> getAllTrainings() {
        return ResponseEntity.ok(trainingService.getAllTrainings());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTraining(@PathVariable UUID id, @RequestBody TrainingDTO TrainingDTO) {
        try {
            TrainingDTO response = trainingService.updateTraining(id, TrainingDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/Trainings/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID id) {
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }
}