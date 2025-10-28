package com.sportbuddy.dto;

public class ParticipantDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String telegramUsername;

    public ParticipantDto(String firstName, String lastName, String phone, String telegramUsername) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.telegramUsername = telegramUsername;
    }

    // Геттеры
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getTelegramUsername() { return telegramUsername; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}