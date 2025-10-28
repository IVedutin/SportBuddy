package com.sportbuddy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_time_slots")
public class CourtTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "court_id", nullable = false)
    private SportCourt court;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    private Integer price;

    // Конструкторы
    public CourtTimeSlot() {}

    public CourtTimeSlot(SportCourt court, LocalDateTime startTime, LocalDateTime endTime, Integer price) {
        this.court = court;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SportCourt getCourt() { return court; }
    public void setCourt(SportCourt court) { this.court = court; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}