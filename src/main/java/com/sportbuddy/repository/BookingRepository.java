package com.sportbuddy.repository;

import com.sportbuddy.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCourtTimeSlotId(Long timeSlotId);
    Integer countByCourtTimeSlotId(Long timeSlotId);
    boolean existsByUserIdAndCourtTimeSlotId(Long userId, Long timeSlotId);
    List<Booking> findByUserId(Long userId);
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.user.id = :userId " +
            "AND b.courtTimeSlot.startTime < :newEndTime " +
            "AND b.courtTimeSlot.endTime > :newStartTime")
    boolean existsOverlappingBooking(@Param("userId") Long userId,
                                     @Param("newStartTime") LocalDateTime newStartTime,
                                     @Param("newEndTime") LocalDateTime newEndTime);
}