package com.crt.server.dto;

import com.crt.server.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private UUID id;
    private String block;
    private String floor;
    private String roomNumber;
    private String subRoom;
    private RoomType roomType;
    private Integer capacity;
    private String roomString;
}