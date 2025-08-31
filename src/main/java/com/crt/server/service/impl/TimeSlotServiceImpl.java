package com.crt.server.service.impl;

import com.crt.server.dto.*;
import com.crt.server.model.*;
import com.crt.server.repository.*;
import com.crt.server.service.CsvService;
import com.crt.server.service.RoomService;
import com.crt.server.service.SectionService;
import com.crt.server.service.TimeSlotService;
import com.crt.server.service.TimeSlotValidationService;
import com.crt.server.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SectionScheduleRepository sectionScheduleRepository;
    private final TrainingRepository trainingRepository;
    private final TimeSlotValidationService validationService;
    private final CsvService csvService;
    private final UserService userService;
    private final RoomService roomService;
    private final SectionService sectionService;

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public TimeSlotDTO createTimeSlot(TimeSlotDTO timeSlotDTO) {
        log.debug("Creating time slot: {}", timeSlotDTO);

        // Validate the time slot
        TimeSlotValidationResponseDTO validation = validationService.validateTimeSlot(timeSlotDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Time slot validation failed: " + validation.getMessage());
        }

        return createTimeSlotInternal(timeSlotDTO);
    }

    @Override
    public TimeSlotDTO getTimeSlot(Integer id) {
        log.debug("Getting time slot with id: {}", id);
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + id));
        return convertToDTO(timeSlot);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public TimeSlotDTO updateTimeSlot(Integer id, TimeSlotDTO timeSlotDTO) {
        log.debug("Updating time slot with id: {}, data: {}", id, timeSlotDTO);

        TimeSlot existingTimeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found with id: " + id));

        // Validate the update
        TimeSlotValidationResponseDTO validation = validationService.validateTimeSlotUpdate(id, timeSlotDTO);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Time slot update validation failed: " + validation.getMessage());
        }

        // Update fields
        updateTimeSlotFields(existingTimeSlot, timeSlotDTO);

        TimeSlot savedTimeSlot = timeSlotRepository.save(existingTimeSlot);
        return convertToDTO(savedTimeSlot);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public void deleteTimeSlot(Integer id) {
        log.debug("Deleting time slot with id: {}", id);

        if (!timeSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("TimeSlot not found with id: " + id);
        }

        timeSlotRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "timeSlotsByType", key = "#slotType")
    public List<TimeSlotDTO> getTimeSlotsByType(TimeSlotType slotType) {
        log.debug("Getting time slots by type: {}", slotType);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySlotType(slotType);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getSectionTimeSlotsByType(UUID sectionId, TimeSlotType slotType) {
        log.debug("Getting section time slots by type: sectionId={}, slotType={}", sectionId, slotType);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionIdAndSlotType(sectionId, slotType);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByDay(DayOfWeek dayOfWeek) {
        log.debug("Getting time slots by day: {}", dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findByDayOfWeek(dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getFacultyTimeSlotsByDay(UUID facultyId, DayOfWeek dayOfWeek) {
        log.debug("Getting faculty time slots by day: facultyId={}, dayOfWeek={}", facultyId, dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findByInchargeFacultyIdAndDayOfWeek(facultyId, dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getSectionTimeSlotsByDay(UUID sectionId, DayOfWeek dayOfWeek) {
        log.debug("Getting section time slots by day: sectionId={}, dayOfWeek={}", sectionId, dayOfWeek);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionIdAndDayOfWeek(sectionId, dayOfWeek);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "timeSlotsBySection", key = "#sectionId")
    public List<TimeSlotDTO> getTimeSlotsBySection(UUID sectionId) {
        log.debug("Getting time slots by section: {}", sectionId);
        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionId(sectionId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "timeSlotsByFaculty", key = "#facultyId")
    public List<TimeSlotDTO> getTimeSlotsByFaculty(UUID facultyId) {
        log.debug("Getting time slots by faculty: {}", facultyId);
        List<TimeSlot> timeSlots = timeSlotRepository.findByInchargeFacultyId(facultyId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsBySection(UUID sectionId) {
        log.debug("Getting active time slots by section: {}", sectionId);
        List<TimeSlot> timeSlots = timeSlotRepository.findActiveBySectionId(sectionId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getActiveTimeSlotsByFaculty(UUID facultyId) {
        log.debug("Getting active time slots by faculty: {}", facultyId);
        List<TimeSlot> timeSlots = timeSlotRepository.findActiveByFacultyId(facultyId);
        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlot(TimeSlotDTO timeSlotDTO) {
        return validationService.validateTimeSlot(timeSlotDTO);
    }

    @Override
    public TimeSlotValidationResponseDTO validateTimeSlotUpdate(Integer timeSlotId, TimeSlotDTO timeSlotDTO) {
        return validationService.validateTimeSlotUpdate(timeSlotId, timeSlotDTO);
    }

    @Override
    public List<TimeSlotDTO> getConflictingTimeSlots(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime) {
        log.debug("Getting conflicting time slots: roomId={}, dayOfWeek={}, startTime={}, endTime={}",
                roomId, dayOfWeek, startTime, endTime);
        List<TimeSlot> conflicts = validationService.getConflictingTimeSlots(roomId, dayOfWeek, startTime, endTime, null);
        return conflicts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTimeSlotAvailable(UUID roomId, DayOfWeek dayOfWeek, String startTime, String endTime) {
        return !timeSlotRepository.existsConflictingTimeSlot(roomId, dayOfWeek, startTime, endTime);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"timeSlotsBySection", "timeSlotsByFaculty"}, allEntries = true)
    public List<TimeSlotDTO> createTimeSlots(List<TimeSlotDTO> timeSlotDTOs) {
        log.debug("Creating {} time slots", timeSlotDTOs.size());

        return timeSlotDTOs.stream()
                .map(this::createTimeSlot)
                .collect(Collectors.toList());
    }

    // Legacy methods for backward compatibility
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "facultyTimeSlots", key = "#faculty.id")
    public List<TimeSlot> findByInchargeFaculty(User faculty) {
        return timeSlotRepository.findByInchargeFaculty(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSlotDTO getTimeSlotById(Integer id) {
        return getTimeSlot(id);
    }

    private TimeSlotDTO createTimeSlotInternal(TimeSlotDTO timeSlotDTO) {
        TimeSlot timeSlot = convertToEntity(timeSlotDTO);
        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToDTO(savedTimeSlot);
    }

    private void updateTimeSlotFields(TimeSlot existingTimeSlot, TimeSlotDTO timeSlotDTO) {
        existingTimeSlot.setStartTime(timeSlotDTO.getStartTime());
        existingTimeSlot.setEndTime(timeSlotDTO.getEndTime());
        existingTimeSlot.setSlotType(timeSlotDTO.getSlotType());
        existingTimeSlot.setTitle(timeSlotDTO.getTitle());
        existingTimeSlot.setDescription(timeSlotDTO.getDescription());
        existingTimeSlot.setDayOfWeek(timeSlotDTO.getDayOfWeek());

        // Update relationships if changed
        if (!existingTimeSlot.getInchargeFaculty().getId().equals(timeSlotDTO.getInchargeFacultyId())) {
            User faculty = userRepository.findById(timeSlotDTO.getInchargeFacultyId())
                    .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
            existingTimeSlot.setInchargeFaculty(faculty);
        }

        if (!existingTimeSlot.getSection().getId().equals(timeSlotDTO.getSectionId())) {
            Section section = sectionRepository.findById(timeSlotDTO.getSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found"));
            existingTimeSlot.setSection(section);
        }

        if (!existingTimeSlot.getRoom().getId().equals(timeSlotDTO.getRoomId())) {
            Room room = roomRepository.findById(timeSlotDTO.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found"));
            existingTimeSlot.setRoom(room);
        }
    }

    private TimeSlot convertToEntity(TimeSlotDTO dto) {
        User faculty = userRepository.findById(dto.getInchargeFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // Get the section schedule - use first available if multiple exist
        SectionSchedule schedule = sectionScheduleRepository.findFirstBySectionId(dto.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("Section schedule not found for section: " + dto.getSectionId()));

        return TimeSlot.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .slotType(dto.getSlotType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dayOfWeek(dto.getDayOfWeek())
                .inchargeFaculty(faculty)
                .section(section)
                .room(room)
                .schedule(schedule)
                .build();
    }

    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .slotType(timeSlot.getSlotType())
                .title(timeSlot.getTitle())
                .description(timeSlot.getDescription())
                .dayOfWeek(timeSlot.getDayOfWeek())
                .inchargeFacultyId(timeSlot.getInchargeFaculty().getId())
                .inchargeFacultyName(timeSlot.getInchargeFaculty().getName())
                .inchargeFacultyEmail(timeSlot.getInchargeFaculty().getEmail())
                .inchargeFacultyPhone(timeSlot.getInchargeFaculty().getPhone())
                .sectionId(timeSlot.getSection().getId())
                .sectionName(timeSlot.getSection().getName())
                .roomId(timeSlot.getRoom().getId())
                .roomName(timeSlot.getRoom().toString())
                .build();
    }

    @Override
    public TimetableUploadResponseDTO bulkCreateTimetable(MultipartFile file) throws Exception {
        log.info("Starting bulk timetable upload from file: {}", file.getOriginalFilename());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<TimetableUploadResponseDTO.SectionTimetableDTO> processedSections = new ArrayList<>();

        int processedSectionCount = 0;
        int createdTimeSlotCount = 0;
        int skippedEntries = 0;

        DayOfWeek dayOfWeek = null;

        List<CSVRecord> records = csvService.parseCsvWithoutHeaders(file);
        try {
            // Parse CSV without headers since the timetable has a custom format

            // Skip the first row (title row)
            if (records.size() < 2) {
                throw new IllegalArgumentException("Invalid timetable format - insufficient data");
            }

            CSVRecord headerRow = records.get(1); // DAY,SECTIONS,Class,ROOMNO,...

            for (int i = 2; i < records.size(); i++) { // Start from row 2 (first data row)
                CSVRecord record = records.get(i);

                try {
                    // Parse morning session
                    if (record.size() >= 8) {
                        String day = record.get(0).trim();
                        if (!day.isEmpty()) {
                            dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
                        }

                        String sectionName = record.get(1).trim();
                        String program = record.get(2).trim();
                        String roomCode = record.get(3).trim();

                        if (!sectionName.isEmpty() && !program.isEmpty() && !roomCode.isEmpty()) {
                            TimetableUploadResponseDTO.SectionTimetableDTO sectionResult =
                                    processSectionTimetableTransactional(sectionName, program, roomCode, record, 4, 8, dayOfWeek, errors, warnings);

                            if (sectionResult != null) {
                                processedSections.add(sectionResult);
                                processedSectionCount++;
                                createdTimeSlotCount += sectionResult.getTimeSlots().size();
                            } else {
                                skippedEntries++;
                            }
                        }
                    }

                    // Parse afternoon session
                    if (record.size() >= 16) {
                        String sectionName = record.get(9).trim();
                        String program = record.get(10).trim();
                        String roomCode = record.get(11).trim();

                        if (!sectionName.isEmpty() && !program.isEmpty() && !roomCode.isEmpty()) {
                            TimetableUploadResponseDTO.SectionTimetableDTO sectionResult =
                                    processSectionTimetableTransactional(sectionName, program, roomCode, record, 12, 16, dayOfWeek, errors, warnings);

                            if (sectionResult != null) {
                                processedSections.add(sectionResult);
                                processedSectionCount++;
                                createdTimeSlotCount += sectionResult.getTimeSlots().size();
                            } else {
                                skippedEntries++;
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i, e.getMessage());
                    warnings.add("Row " + i + " skipped: " + e.getMessage());
                    skippedEntries++;
                }
            }

        } catch (Exception e) {
            log.error("Error parsing timetable CSV: {}", e.getMessage());
            throw new RuntimeException("Failed to parse timetable: " + e.getMessage(), e);
        }

        log.info("Bulk timetable upload completed. Processed: {}, Created: {}, Skipped: {}",
                processedSectionCount, createdTimeSlotCount, skippedEntries);

        return TimetableUploadResponseDTO.builder()
                .processedSections(processedSectionCount)
                .createdTimeSlots(createdTimeSlotCount)
                .skippedEntries(skippedEntries)
                .dayOfWeek(dayOfWeek)
                .sections(processedSections)
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    @Override
    public TimetableUploadResponseDTO.SectionTimetableDTO processSectionTimetableTransactional(
            String sectionName, String program, String roomCode, CSVRecord record,
            int startCol, int endCol, DayOfWeek dayOfWeek, List<String> errors, List<String> warnings) {
        
        try {
            return processSectionTimetable(sectionName, program, roomCode, record, startCol, endCol, dayOfWeek, errors, warnings);
        } catch (Exception e) {
            log.error("Transaction error processing section {}: {}", sectionName, e.getMessage());
            log.error("Exception type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
            warnings.add("Transaction error processing section " + sectionName + ": " + e.getMessage());
            return null;
        }
    }

    private TimetableUploadResponseDTO.SectionTimetableDTO processSectionTimetable(
            String sectionName, String program, String roomCode, CSVRecord record,
            int startCol, int endCol, DayOfWeek dayOfWeek, List<String> errors, List<String> warnings) {

        try {
            // Get section with fallback
            log.debug("Looking up section with name: '{}'", sectionName);
            Section section = null;
            try {
                section = sectionService.getSectionByName(sectionName);
            } catch (Exception e) {
                log.error("Exception during section lookup for '{}': {}", sectionName, e.getMessage());
            }
            
            if (section == null) {
                log.warn("Section '{}' not found, creating new section", sectionName);
                // Create new section instead of using random fallback
                try {
                    section = createNewSection(sectionName, program, warnings);
                    log.info("✅ Created new section: {} for program: {}", sectionName, program);
                } catch (Exception createException) {
                    log.error("Failed to create new section '{}': {}", sectionName, createException.getMessage());
                    // Fallback to random existing section only if creation fails
                    List<SectionDTO> allSections = sectionService.getAllSections();
                    log.debug("Available sections for fallback: {}", allSections.size());
                    if (allSections.isEmpty()) {
                        warnings.add("No sections available in the system for fallback");
                        return null;
                    }
                    // Get random section and fetch it directly from repository
                    SectionDTO randomSectionDTO = allSections.get((int) (Math.random() * allSections.size()));
                    log.debug("Selected random section DTO: {} (ID: {})", randomSectionDTO.getName(), randomSectionDTO.getId());
                    section = sectionRepository.findById(randomSectionDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Fallback section not found"));
                    warnings.add("Section '" + sectionName + "' creation failed. Using fallback section: " + section.getName());
                    log.warn("Section '{}' creation failed. Using fallback section: {}", sectionName, section.getName());
                }
            } else {
                log.debug("Found section: {} (ID: {})", section.getName(), section.getId());
            }

            // Get room with fallback
            Room room;
            try {
                log.debug("Looking up room with code: '{}'", roomCode);
                room = roomRepository.findByRoomCode(roomCode);
                if (room == null) {
                    // Fallback: Get a random room
                    List<RoomDTO> allRooms = roomService.getAllRooms();
                    if (allRooms.isEmpty()) {
                        warnings.add("No rooms available in the system for fallback");
                        return null;
                    }
                    // Get random room
                    RoomDTO randomRoomDTO = allRooms.get((int) (Math.random() * allRooms.size()));
                    room = roomRepository.findById(randomRoomDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Fallback room not found"));
                    warnings.add("Room '" + roomCode + "' not found for section " + sectionName + ". Using fallback room: " + room.toString());
                    log.warn("Room '{}' not found for section {}. Using fallback room: {}", roomCode, sectionName, room.toString());
                } else {
                    log.debug("Found room: {} (ID: {}) for section: {}", room.toString(), room.getId(), sectionName);
                }
            } catch (Exception e) {
                log.error("Error finding room '{}' for section {}: {}", roomCode, sectionName, e.getMessage());
                // Try fallback even on exception
                try {
                    List<RoomDTO> allRooms = roomService.getAllRooms();
                    if (allRooms.isEmpty()) {
                        warnings.add("No rooms available in the system for fallback after error: " + e.getMessage());
                        return null;
                    }
                    RoomDTO randomRoomDTO = allRooms.get((int) (Math.random() * allRooms.size()));
                    room = roomRepository.findById(randomRoomDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Fallback room not found"));
                    warnings.add("Error finding room '" + roomCode + "' for section " + sectionName + ". Using fallback room: " + room.toString());
                    log.warn("Error finding room '{}' for section {}. Using fallback room: {}", roomCode, sectionName, room.toString());
                } catch (Exception fallbackException) {
                    warnings.add("Error finding room '" + roomCode + "' and fallback failed: " + fallbackException.getMessage());
                    return null;
                }
            }

            // Comprehensive safety checks
            if (room == null) {
                log.error("CRITICAL: Room is null for section {} at time slot creation", sectionName);
                warnings.add("Room is null for section " + sectionName + " - skipping time slot");
                return null;
            }
            
            if (room.getId() == null) {
                log.error("CRITICAL: Room ID is null for section {} (room: {})", sectionName, room);
                warnings.add("Room ID is null for section " + sectionName + " - skipping time slot");
                return null;
            }
            
            log.debug("Creating TimeSlot for section {} with room {} (ID: {})", 
                     sectionName, room.toString(), room.getId());

            SectionSchedule schedule = getOrCreateSectionSchedule(section, room, dayOfWeek);

            String[] morningTimes = {"09:20-10:10", "10:10-11:00", "11:10-12:00", "12:00-12:50"};
            String[] afternoonTimes = {"01:50-02:40", "02:40-03:30", "03:40-04:30", "04:30-05:20"};
            String[] timeSlots = (startCol == 4) ? morningTimes : afternoonTimes;

            List<TimetableUploadResponseDTO.TimeSlotSummaryDTO> createdSlots = new ArrayList<>();
            List<TimeSlot> newTimeSlots = new ArrayList<>(); // Collect new time slots first

            // First, validate all time slots and collect them
            boolean allSlotsValid = true;

            // Process each time slot
            for (int i = startCol; i < endCol && i < record.size(); i++) {
                String facultyData = record.get(i).trim();
                if (facultyData.isEmpty()) continue;

                String timeRange = timeSlots[i - startCol];
                String[] timeParts = timeRange.split("-");
                String startTime = timeParts[0];
                String endTime = timeParts[1];

                // Parse faculty assignment
                String facultyEmpId;
                boolean isExam = false;

                if (facultyData.startsWith("EXAM-")) {
                    facultyEmpId = facultyData.substring(5); // Remove "EXAM-" prefix
                    isExam = true;
                } else {
                    facultyEmpId = facultyData;
                }

                try {
                    User faculty;
                    try {
                        faculty = userService.getUserByEmployeeId(facultyEmpId);
                    } catch (Exception e) {
                        // Fallback: Get a random faculty
                        List<UserDTO> allFaculties = userService.getAllFacs();
                        if (allFaculties.isEmpty()) {
                            warnings.add("No faculties available in the system for fallback (empId: " + facultyEmpId + ")");
                            allSlotsValid = false;
                            break;
                        }
                        // Get random faculty
                        UserDTO randomFacultyDTO = allFaculties.get((int) (Math.random() * allFaculties.size()));
                        faculty = userService.getFacById(randomFacultyDTO.getId());
                        warnings.add("Faculty with empId '" + facultyEmpId + "' not found for section " + sectionName + 
                                   ". Using fallback faculty: " + faculty.getName() + " (" + faculty.getEmployeeId() + ")");
                        log.warn("Faculty with empId '{}' not found for section {}. Using fallback faculty: {} ({})", 
                                facultyEmpId, sectionName, faculty.getName(), faculty.getEmployeeId());
                    }

                    // Comprehensive safety checks
                    if (room == null) {
                        log.error("CRITICAL: Room is null for section {} at time slot creation", sectionName);
                        warnings.add("Room is null for section " + sectionName + " - skipping time slot");
                        allSlotsValid = false;
                        break;
                    }
                    
                    if (room.getId() == null) {
                        log.error("CRITICAL: Room ID is null for section {} (room: {})", sectionName, room);
                        warnings.add("Room ID is null for section " + sectionName + " - skipping time slot");
                        allSlotsValid = false;
                        break;
                    }
                    
                    log.debug("Creating TimeSlot for section {} with room {} (ID: {})", 
                             sectionName, room.toString(), room.getId());

                    // Create TimeSlot (but don't save yet)
                    TimeSlot timeSlot = TimeSlot.builder()
                            .startTime(startTime)
                            .endTime(endTime)
                            .slotType(isExam ? TimeSlotType.EXAM : TimeSlotType.REGULAR)
                            .title(program)
                            .dayOfWeek(dayOfWeek)
                            .inchargeFaculty(faculty)
                            .section(section)
                            .room(room)
                            .schedule(schedule)
                            .build();

                    newTimeSlots.add(timeSlot);

                    createdSlots.add(TimetableUploadResponseDTO.TimeSlotSummaryDTO.builder()
                            .startTime(startTime)
                            .endTime(endTime)
                            .facultyEmpId(facultyEmpId)
                            .title(program)
                            .isExam(isExam)
                            .slotType(timeSlot.getSlotType().name())
                            .build());

                } catch (Exception e) {
                    log.error("Critical error processing faculty for empId {} in section {}: {}", 
                             facultyEmpId, sectionName, e.getMessage());
                    warnings.add("Critical error processing faculty " + facultyEmpId + " in section " + sectionName + ": " + e.getMessage());
                    allSlotsValid = false;
                    break;
                }
            }

            // Only proceed with database operations if all slots are valid
            if (allSlotsValid && !newTimeSlots.isEmpty()) {
                try {
                    // Log what we're about to save
                    log.info("=== ABOUT TO SAVE TIME SLOTS FOR SECTION: {} ===", sectionName);
                    log.info("Section: {} (ID: {})", section.getName(), section.getId());
                    log.info("Room: {} (ID: {})", room.toString(), room.getId());
                    log.info("Day: {}", dayOfWeek);
                    log.info("Number of time slots to save: {}", newTimeSlots.size());
                    
                    for (int i = 0; i < newTimeSlots.size(); i++) {
                        TimeSlot slot = newTimeSlots.get(i);
                        log.info("TimeSlot {}: {} - {} | Faculty: {} ({}) | Type: {}", 
                                i + 1,
                                slot.getStartTime(), 
                                slot.getEndTime(),
                                slot.getInchargeFaculty().getName(),
                                slot.getInchargeFaculty().getEmployeeId(),
                                slot.getSlotType());
                    }
                    log.info("=== END OF TIME SLOT PREVIEW ===");
                    
                    // Save time slots using separate transaction
                    try {
                        saveTimeSlotsTransactional(section, dayOfWeek, newTimeSlots, warnings, sectionName);
                        log.info("✅ Successfully saved {} time slots for section {}", newTimeSlots.size(), sectionName);
                    } catch (Exception saveException) {
                        log.error("❌ Database error saving time slots for section {}: {}", sectionName, saveException.getMessage());
                        log.error("❌ Exception type: {}", saveException.getClass().getSimpleName());
                        if (saveException.getCause() != null) {
                            log.error("❌ Root cause: {}", saveException.getCause().getMessage());
                        }
                        // Log the full stack trace for debugging
                        log.error("❌ Full stack trace:", saveException);
                        warnings.add("Database error saving time slots for section " + sectionName + ": " + saveException.getMessage());
                        return null;
                    }
                } catch (Exception e) {
                    log.error("❌ Error saving time slots for section {}: {}", sectionName, e.getMessage());
                    warnings.add("Error saving time slots for section " + sectionName + ": " + e.getMessage());
                    return null;
                }
            } else {
                log.warn("⚠️ Skipping section {} due to validation errors or no time slots", sectionName);
                return null;
            }

            return TimetableUploadResponseDTO.SectionTimetableDTO.builder()
                    .sectionName(sectionName)
                    .roomCode(roomCode)
                    .program(program)
                    .timeSlots(createdSlots)
                    .build();

        } catch (Exception e) {
            warnings.add("Error processing section " + sectionName + ": " + e.getMessage());
            return null;
        }
    }

    private SectionSchedule getOrCreateSectionSchedule(Section section, Room room, DayOfWeek dayOfWeek) {
        // Safety checks
        if (section == null) {
            throw new IllegalArgumentException("Section cannot be null");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        if (room.getId() == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        log.debug("Getting or creating section schedule for section: {} ({}), room: {} ({}), day: {}", 
                 section.getName(), section.getId(), room.toString(), room.getId(), dayOfWeek);
        
        // Try to find existing schedule for this section and day
        List<SectionSchedule> existingSchedules = sectionScheduleRepository.findBySection(section);

        for (SectionSchedule schedule : existingSchedules) {
            // Check if this schedule already has time slots for this day
            List<TimeSlot> daySlots = timeSlotRepository.findByScheduleAndDayOfWeek(schedule, dayOfWeek);
            if (daySlots.isEmpty()) {
                log.debug("Reusing existing schedule: {}", schedule.getId());
                return schedule; // Reuse existing schedule
            }
        }

        // Create new schedule if none found or all are occupied
        log.debug("Creating new section schedule for section: {}, room: {}", section.getName(), room.toString());
        SectionSchedule newSchedule = SectionSchedule.builder()
                .section(section)
                .room(room)
                .build();

        SectionSchedule savedSchedule = sectionScheduleRepository.save(newSchedule);
        log.debug("Created new section schedule with ID: {}", savedSchedule.getId());
        return savedSchedule;
    }

    private Section createNewSection(String sectionName, String program, List<String> warnings) {
        try {
            // Try to find a training that matches the program
            Training training = null;
            try {
                // First try to find by program name
                training = trainingRepository.findByName(program);
                if (training == null) {
                    // Try to find by short name
                    training = trainingRepository.findBySn(program);
                }
            } catch (Exception e) {
                log.debug("Error finding training for program '{}': {}", program, e.getMessage());
            }
            
            // If no training found, create a default one or use first available
            if (training == null) {
                List<Training> allTrainings = trainingRepository.findAll();
                if (!allTrainings.isEmpty()) {
                    training = allTrainings.get(0); // Use first available training
                    warnings.add("Training '" + program + "' not found for section " + sectionName + ". Using default training: " + training.getName());
                } else {
                    throw new RuntimeException("No trainings available in the system to create section");
                }
            }
            
            // Create new section
            Section newSection = Section.builder()
                    .name(sectionName)
                    .training(training)
                    .strength(0)
                    .capacity(30) // Default capacity
                    .build();
            
            Section savedSection = sectionRepository.save(newSection);
            warnings.add("Created new section: " + sectionName + " for training: " + training.getName());
            return savedSection;
            
        } catch (Exception e) {
            log.error("Failed to create new section '{}': {}", sectionName, e.getMessage());
            throw new RuntimeException("Failed to create section: " + sectionName, e);
        }
    }

    @Override
    public SectionDayScheduleDTO getSectionScheduleByDay(String sectionName, DayOfWeek dayOfWeek) {
        Section section = sectionService.getSectionByName(sectionName);
        if (section == null) {
            throw new EntityNotFoundException("Section not found: " + sectionName);
        }
        return getSectionScheduleByDay(section.getId(), dayOfWeek);
    }

    @Override
    public SectionDayScheduleDTO getSectionScheduleByDay(UUID sectionId, DayOfWeek dayOfWeek) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        List<TimeSlot> timeSlots = timeSlotRepository.findBySectionAndDayOfWeek(section, dayOfWeek);

        if (timeSlots.isEmpty()) {
            return SectionDayScheduleDTO.builder()
                    .sectionName(section.getName())
                    .sectionId(sectionId.toString())
                    .dayOfWeek(dayOfWeek)
                    .timeSlots(new ArrayList<>())
                    .totalSlots(0)
                    .build();
        }

        // Get room info from first time slot (assuming all slots use same room for the day)
        Room room = timeSlots.get(0).getRoom();

        List<SectionDayScheduleDTO.TimeSlotDetailDTO> slotDetails = timeSlots.stream()
                .map(slot -> SectionDayScheduleDTO.TimeSlotDetailDTO.builder()
                        .timeSlotId(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .title(slot.getTitle())
                        .description(slot.getDescription())
                        .slotType(slot.getSlotType().name())
                        .isExam(slot.getSlotType() == TimeSlotType.EXAM)
                        .faculty(SectionDayScheduleDTO.FacultyInfoDTO.builder()
                                .empId(slot.getInchargeFaculty().getEmployeeId())
                                .name(slot.getInchargeFaculty().getName())
                                .email(slot.getInchargeFaculty().getEmail())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return SectionDayScheduleDTO.builder()
                .sectionName(section.getName())
                .sectionId(sectionId.toString())
                .dayOfWeek(dayOfWeek)
                .roomCode(room.getBlock() + room.getFloor() + room.getRoomNumber() +
                        (room.getSubRoom() != null ? room.getSubRoom() : ""))
                .roomName(room.toString())
                .timeSlots(slotDetails)
                .totalSlots(slotDetails.size())
                .build();
    }

    @Transactional
    protected void saveTimeSlotsTransactional(Section section, DayOfWeek dayOfWeek, List<TimeSlot> newTimeSlots,
                                            List<String> warnings, String sectionName) {
        saveTimeSlots(section, dayOfWeek, newTimeSlots, warnings, sectionName);
    }

    protected void saveTimeSlots(Section section, DayOfWeek dayOfWeek, List<TimeSlot> newTimeSlots,
                                 List<String> warnings, String sectionName) {
        
        // Validate all time slots before saving
        for (TimeSlot slot : newTimeSlots) {
            if (slot.getSection() == null || slot.getSection().getId() == null) {
                throw new IllegalStateException("TimeSlot section is null for section: " + sectionName);
            }
            if (slot.getRoom() == null || slot.getRoom().getId() == null) {
                throw new IllegalStateException("TimeSlot room is null for section: " + sectionName);
            }
            if (slot.getInchargeFaculty() == null || slot.getInchargeFaculty().getId() == null) {
                throw new IllegalStateException("TimeSlot faculty is null for section: " + sectionName);
            }
            if (slot.getSchedule() == null || slot.getSchedule().getId() == null) {
                throw new IllegalStateException("TimeSlot schedule is null for section: " + sectionName);
            }
        }
        
        try {
            // Delete existing time slots for this section on this day (replace them)
            List<TimeSlot> existingSlots = timeSlotRepository.findBySectionAndDayOfWeek(section, dayOfWeek);
            if (!existingSlots.isEmpty()) {
                log.debug("Deleting {} existing time slots for section {} on {}", existingSlots.size(), sectionName, dayOfWeek);
                timeSlotRepository.deleteAll(existingSlots);
                warnings.add("Replaced " + existingSlots.size() + " existing time slots for section " + sectionName);
            }
            
            // Save all new time slots
            log.debug("Saving {} new time slots for section {} on {}", newTimeSlots.size(), sectionName, dayOfWeek);
            timeSlotRepository.saveAll(newTimeSlots);
            log.debug("Successfully saved {} time slots for section {}", newTimeSlots.size(), sectionName);
            
        } catch (Exception e) {
            log.error("Database error in saveTimeSlots for section {}: {}", sectionName, e.getMessage());
            log.error("Exception type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
                log.error("Root cause type: {}", e.getCause().getClass().getSimpleName());
            }
            throw e; // Re-throw to be caught by caller
        }
    }
}
