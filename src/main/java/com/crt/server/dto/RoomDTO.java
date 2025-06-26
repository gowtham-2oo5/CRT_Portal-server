package com.crt.server.dto;

import com.crt.server.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private UUID id;
    
    @NotBlank(message = "Block is required")
    private String block;
    
    @NotBlank(message = "Floor is required")
    private String floor;
    
    @NotBlank(message = "Room number is required")
    private String roomNumber;
    
    private String subRoom;
    
    @NotNull(message = "Room type is required")
    private RoomType roomType;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private String roomString;
}
