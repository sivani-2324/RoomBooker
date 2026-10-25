package com.roombooker.roombooker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserName(String userName);

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.roomId = :roomId
          AND b.bookingDatetime = :bookingDate
          AND b.status = 'Booked'
          AND (
              :fullDay = true
              OR b.fullDay = true
              OR (
                  b.startTime < :endTime
                  AND b.endTime > :startTime
              )
          )
        """)
    boolean hasOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("bookingDate") String bookingDate,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("fullDay") boolean fullDay
    );
}