package com.crt.server.controller;

import com.crt.server.dto.TrainerDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    public ResponseEntity<?> createTrainer(@RequestBody TrainerDTO trainerDTO) {
        try {
            TrainerDTO response = trainerService.createTrainer(trainerDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.CONFLICT.value())
                    .error(HttpStatus.CONFLICT.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/trainers")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable UUID id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<TrainerDTO> getTrainerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(trainerService.getTrainerByEmail(email));
    }

    @GetMapping("/sn/{sn}")
    public ResponseEntity<TrainerDTO> getTrainerBySn(@PathVariable String sn) {
        return ResponseEntity.ok(trainerService.getTrainerBySn(sn));
    }

    @GetMapping
    public ResponseEntity<List<TrainerDTO>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAllTrainers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrainer(@PathVariable UUID id, @RequestBody TrainerDTO trainerDTO) {
        try {
            TrainerDTO response = trainerService.updateTrainer(id, trainerDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(e.getMessage())
                    .path("/api/trainers/" + id)
                    .build();
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable UUID id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }
}