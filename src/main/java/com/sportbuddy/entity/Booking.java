package com.sportbuddy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "court_time_id", nullable = false)
    private CourtTimeSlot courtTimeSlot;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Конструкторы
    public Booking() {}

    public Booking(User user, CourtTimeSlot courtTimeSlot) {
        this.user = user;
        this.courtTimeSlot = courtTimeSlot;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CourtTimeSlot getCourtTimeSlot() { return courtTimeSlot; }
    public void setCourtTimeSlot(CourtTimeSlot courtTimeSlot) { this.courtTimeSlot = courtTimeSlot; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}