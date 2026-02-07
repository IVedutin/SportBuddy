package com.sportbuddy.dto;

import java.time.LocalDate;

public class BookingDto {
    private Long id;
    private Long timeSlotId;
    private String sportType;
    private String courtName;
    private String timeSlot;
    private String date;
    private Integer participantCount;
    private Long courtId;

    public BookingDto(Long id, Long timeSlotId, String sportType, String courtName,
                      String timeSlot, String date, Integer participantCount,Long courtId) {
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.sportType = sportType;
        this.courtName = courtName;
        this.timeSlot = timeSlot;
        this.date = date;
        this.participantCount = participantCount;
        this.courtId = courtId;
    }

    // Геттеры
    public Long getId() { return id; }
    public Long getTimeSlotId() { return timeSlotId; }
    public String getSportType() { return sportType; }
    public String getCourtName() { return courtName; }
    public String getTimeSlot() { return timeSlot; }
    public String getDate() { return date; }
    public Integer getParticipantCount() { return participantCount; }
    public Long getCourtId(){return courtId;}
}