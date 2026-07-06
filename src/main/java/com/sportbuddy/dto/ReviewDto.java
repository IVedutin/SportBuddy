package com.sportbuddy.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private String comment;
    private int rating;
    private LocalDateTime createdAt;
    private String userName;

    // Конструктор
    public ReviewDto(Long id, String comment, int rating, LocalDateTime createdAt, String userName) {
        this.id = id;
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
        this.userName = userName;
    }

    // Геттеры (обязательно!)
    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUserName() {
        return userName;
    }

    // Сеттеры (опционально)
    public void setId(Long id) {
        this.id = id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}