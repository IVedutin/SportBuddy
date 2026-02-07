package com.sportbuddy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "sport_courts")
public class SportCourt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @ManyToOne
    @JoinColumn(name = "sport_type_id", nullable = false)
    private SportType sportType;

    private Integer maxPlayers;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @Column(name = "status")
    private String status;

    // --- НОВЫЕ ПОЛЯ ДЛЯ ВРЕМЕНИ ---
    @Column(name = "operating_hours")
    private String operatingHours; // Например, "09:00 - 22:00"

    @Column(name = "working_days")
    private String workingDays; // Например, "Ежедневно"
    // --- КОНЕЦ НОВЫХ ПОЛЕЙ ---


    // Конструкторы
    public SportCourt() {}

    public SportCourt(String name, String address, SportType sportType, Integer maxPlayers) {
        this.name = name;
        this.address = address;
        this.sportType = sportType;
        this.maxPlayers = maxPlayers;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public SportType getSportType() { return sportType; }
    public void setSportType(SportType sportType) { this.sportType = sportType; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

}
