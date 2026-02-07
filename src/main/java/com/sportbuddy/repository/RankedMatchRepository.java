package com.sportbuddy.repository;

import com.sportbuddy.entity.RankedMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface RankedMatchRepository extends JpaRepository<RankedMatch, Long> {
    // Найти будущие матчи
    List<RankedMatch> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);
}
