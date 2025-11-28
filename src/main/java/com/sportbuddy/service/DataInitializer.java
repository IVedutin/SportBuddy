package com.sportbuddy.service;

import com.sportbuddy.entity.Booking;
import com.sportbuddy.entity.SportType;
import com.sportbuddy.entity.SportCourt;
import com.sportbuddy.entity.CourtTimeSlot;
import com.sportbuddy.repository.BookingRepository;
import com.sportbuddy.repository.SportTypeRepository;
import com.sportbuddy.repository.SportCourtRepository;
import com.sportbuddy.repository.CourtTimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportCourtRepository sportCourtRepository;

    @Autowired
    private CourtTimeSlotRepository courtTimeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository; // Добавьте это

    @Override
    public void run(String... args) throws Exception {
        // Добавляем виды спорта если их нет
        if (sportTypeRepository.count() == 0) {
            SportType football = new SportType("Футбол", "Футбольные игры");
            SportType basketball = new SportType("Баскетбол", "Баскетбольные игры");
            SportType tennis = new SportType("Теннис", "Теннисные корты");

            sportTypeRepository.saveAll(Arrays.asList(football, basketball, tennis));

            // Добавляем площадки для футбола
            SportCourt footballCourt1 = new SportCourt("ФОК Звездный", "ул. Большая Затонская, 3", football, 30);
            SportCourt footballCourt2 = new SportCourt("ФОК Юбилейный", "ул, Братьев Никитиных, 10", football, 30);
            SportCourt footballCourt3 = new SportCourt("Площадка у СГЮА","ул, Чернышевского 104",football,30);

            // Добавляем площадки для баскетбола
            SportCourt basketballCourt1 = new SportCourt("ФОК Звездный", "ул. Большая Затонская, 3", basketball, 20);
            SportCourt basketballCourt2 = new SportCourt("ФОК Юбилейный", "ул, Братьев Никитиных, 10", basketball, 20);
            SportCourt basketballCourt3 = new SportCourt("Площадка у СГЮА","ул, Чернышевского 104",basketball,20);

            // Добавляем площадки для тенниса
            SportCourt tennisCourt1 = new SportCourt("Ракета", "ул. Астрахансккая, 103", tennis, 4);
            SportCourt tennisCourt2 = new SportCourt("Первая школа тенниса", "ул. Чернышевского, 94", tennis, 2);

            sportCourtRepository.saveAll(Arrays.asList(
                    footballCourt1, footballCourt2, footballCourt3,
                    basketballCourt1, basketballCourt2, basketballCourt3,
                    tennisCourt1, tennisCourt2
            ));
        }

        // ВСЕГДА обновляем слоты при запуске
        updateTimeSlotsForAllCourts();
    }

    private void updateTimeSlotsForAllCourts() {
        List<SportCourt> allCourts = sportCourtRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);

        System.out.println("=== UPDATING TIME SLOTS ===");
        System.out.println("Today: " + today);
        System.out.println("Courts count: " + allCourts.size());

        // Удаляем бронирования для прошедших слотов
        List<Booking> oldBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getCourtTimeSlot().getStartTime().isBefore(cutoffTime))
                .collect(Collectors.toList());

        bookingRepository.deleteAll(oldBookings);
        System.out.println("Deleted " + oldBookings.size() + " old bookings");

        // Удаляем прошедшие слоты
        List<CourtTimeSlot> oldSlots = courtTimeSlotRepository.findAll().stream()
                .filter(slot -> slot.getStartTime().isBefore(cutoffTime))
                .collect(Collectors.toList());

        courtTimeSlotRepository.deleteAll(oldSlots);
        System.out.println("Deleted " + oldSlots.size() + " old time slots");

        // Создаем новые слоты на недостающие дни
        for (SportCourt court : allCourts) {
            updateTimeSlotsForCourt(court, today);
        }

        System.out.println("Time slots updated successfully");
    }
    private void updateTimeSlotsForCourt(SportCourt court, LocalDate startDate) {
        System.out.println("Updating slots for court: " + court.getName());

        // Получаем существующие слоты для этой площадки
        List<CourtTimeSlot> existingSlots = courtTimeSlotRepository.findByCourtId(court.getId());

        // Определяем какие дни нужно добавить
        Set<LocalDate> existingDates = existingSlots.stream()
                .map(slot -> slot.getStartTime().toLocalDate())
                .collect(Collectors.toSet());

        for (int day = 0; day < 3; day++) {
            LocalDate currentDate = startDate.plusDays(day);

            // Если слотов на эту дату нет - создаем
            if (!existingDates.contains(currentDate)) {
                System.out.println("Adding slots for " + court.getName() + " on " + currentDate);
                addTimeSlotsForSingleDay(court, currentDate);
            }
        }
    }

    private void addTimeSlotsForSingleDay(SportCourt court, LocalDate date) {
        for (int hour = 6; hour < 22; hour++) {
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, 0));
            LocalDateTime endTime = startTime.plusHours(1);

            CourtTimeSlot slot = new CourtTimeSlot(
                    court,
                    startTime,
                    endTime,
                    0
            );
            courtTimeSlotRepository.save(slot);
        }
    }

    private void addTimeSlotsForCourt(SportCourt court, LocalDate startDate) {
        System.out.println("Creating slots for court: " + court.getName());

        for (int day = 0; day < 3; day++) { // на сегодня, завтра, послезавтра
            LocalDate currentDate = startDate.plusDays(day);

            for (int hour = 6; hour < 22; hour++) { // с 6:00 до 21:00
                LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.of(hour, 0));
                LocalDateTime endTime = startTime.plusHours(1);

                CourtTimeSlot slot = new CourtTimeSlot(
                        court,
                        startTime,
                        endTime,
                        0 // БЕСПЛАТНО!
                );
                courtTimeSlotRepository.save(slot);
            }
        }
        System.out.println("Created slots for court: " + court.getName() + " from " + startDate + " to " + startDate.plusDays(2));
    }
}