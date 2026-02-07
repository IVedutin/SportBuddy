package com.sportbuddy.repository;

import com.sportbuddy.entity.SportCourt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SportCourtRepository extends JpaRepository<SportCourt, Long> {
    List<SportCourt> findBySportTypeIdAndStatus(Long sportTypeId, String status);
    List<SportCourt> findByStatus(String status);
}