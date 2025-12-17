package com.crt.server.controller;

import com.crt.server.dto.TimeSlotDTO;
import com.crt.server.model.TimeSlot;
import com.crt.server.model.User;
import com.crt.server.repository.TimeSlotRepository;
import com.crt.server.repository.UserRepository;
import com.crt.server.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/adjacent-time-slots")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
public class AdjacentTimeSlotController {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    /**
     * Get all adjacent time slots for the current faculty
     * 
     * @return List of pairs of adjacent time slots
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<List<List<TimeSlotDTO>>> getMyAdjacentTimeSlots() {
        User currentUser = currentUserService.getCurrentUser();
        log.info("Getting adjacent time slots for faculty: {}", currentUser.getId());
        
        List<Object[]> adjacentTimeSlots = timeSlotRepository.findAdjacentTimeSlotsByFaculty(currentUser);
        List<List<TimeSlotDTO>> result = new ArrayList<>();
        
        for (Object[] pair : adjacentTimeSlots) {
            TimeSlot ts1 = (TimeSlot) pair[0];
            TimeSlot ts2 = (TimeSlot) pair[1];
            
            List<TimeSlotDTO> timeSlotPair = new ArrayList<>();
            timeSlotPair.add(convertToDTO(ts1));
            timeSlotPair.add(convertToDTO(ts2));
            
            result.add(timeSlotPair);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get all adjacent time slots for a specific faculty
     * 
     * @param facultyId The faculty ID
     * @return List of pairs of adjacent time slots
     */
    @GetMapping("/faculty/{facultyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<List<TimeSlotDTO>>> getAdjacentTimeSlotsForFaculty(@PathVariable UUID facultyId) {
        User faculty = userRepository.findById(facultyId)
                .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
        
        log.info("Getting adjacent time slots for faculty: {}", facultyId);
        
        List<Object[]> adjacentTimeSlots = timeSlotRepository.findAdjacentTimeSlotsByFaculty(faculty);
        List<List<TimeSlotDTO>> result = new ArrayList<>();
        
        for (Object[] pair : adjacentTimeSlots) {
            TimeSlot ts1 = (TimeSlot) pair[0];
            TimeSlot ts2 = (TimeSlot) pair[1];
            
            List<TimeSlotDTO> timeSlotPair = new ArrayList<>();
            timeSlotPair.add(convertToDTO(ts1));
            timeSlotPair.add(convertToDTO(ts2));
            
            result.add(timeSlotPair);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Check if two time slots are adjacent
     * 
     * @param timeSlot1Id First time slot ID
     * @param timeSlot2Id Second time slot ID
     * @return True if the time slots are adjacent, false otherwise
     */
    @GetMapping("/check")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FACULTY')")
    public ResponseEntity<Map<String, Boolean>> checkAdjacentTimeSlots(
            @RequestParam Integer timeSlot1Id,
            @RequestParam Integer timeSlot2Id) {
        
        log.info("Checking if time slots {} and {} are adjacent", timeSlot1Id, timeSlot2Id);
        
        boolean areAdjacent = timeSlotRepository.areTimeSlotsAdjacent(timeSlot1Id, timeSlot2Id);
        
        return ResponseEntity.ok(Map.of("areAdjacent", areAdjacent));
    }
    
    /**
     * Convert TimeSlot entity to TimeSlotDTO
     * 
     * @param timeSlot The time slot entity
     * @return The time slot DTO
     */
    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .isBreak(timeSlot.isBreak())
                .breakDescription(timeSlot.getBreakDescription())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .sectionId(timeSlot.getSection().getId())
                .sectionName(timeSlot.getSection().getName())
                .roomId(timeSlot.getRoom().getId())
                .roomName(timeSlot.getRoom().toString())
                .build();
    }
}
