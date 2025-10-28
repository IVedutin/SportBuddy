package com.sportbuddy.repository;

import com.sportbuddy.entity.CourtTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourtTimeSlotRepository extends JpaRepository<CourtTimeSlot, Long> {
    List<CourtTimeSlot> findByCourtId(Long courtId);
}