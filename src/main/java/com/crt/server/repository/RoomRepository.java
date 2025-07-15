package com.crt.server.repository;

import com.crt.server.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

        @Query("SELECT r FROM Room r WHERE r.block = :block AND r.floor = :floor AND r.roomNumber = :roomNumber")
        Room findByBlockAndFloorAndRoomNumber(
                        @Param("block") String block,
                        @Param("floor") String floor,
                        @Param("roomNumber") String roomNumber);

        @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r WHERE r.block = :block AND r.floor = :floor AND r.roomNumber = :roomNumber AND r.subRoom = :subRoom")
        boolean existsByBlockAndFloorAndRoomNumber(
                @Param("block") String block,
                @Param("floor") String floor,
                @Param("roomNumber") String roomNumber,
                @Param("subRoom")String subRoom);

        @Query("SELECT CASE WHEN COUNT(r) > 0 then TRUE ELSE false END FROM Room r WHERE r.roomNumber = :s")
        Boolean existsByRoomNumber(String s);
}