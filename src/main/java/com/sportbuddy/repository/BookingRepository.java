package com.sportbuddy.repository;

import com.sportbuddy.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCourtTimeSlotId(Long timeSlotId);
    Integer countByCourtTimeSlotId(Long timeSlotId);
    boolean existsByUserIdAndCourtTimeSlotId(Long userId, Long timeSlotId);
    List<Booking> findByUserId(Long userId);
}