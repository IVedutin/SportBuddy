package com.sportbuddy.service;
import lombok.extern.slf4j.Slf4j;

import com.sportbuddy.entity.*;
import com.sportbuddy.repository.*;
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
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired private SportTypeRepository sportTypeRepository;
    @Autowired private SportCourtRepository sportCourtRepository;
    @Autowired private CourtTimeSlotRepository courtTimeSlotRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private CityRepository cityRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Инициализируем города (идемпотентно)
        initCities();

        // 2. Инициализируем виды спорта и площадки только если их нет
        if (sportTypeRepository.count() == 0) {
            initSportTypesAndCourts();
        }

        // 3. Всегда: привязываем площадки без города к Саратову
        assignCityToCourtsMissingCity();

        // 4. Всегда: создаём слоты на недостающие дни (не трогаем старые данные/пользователей)
        updateTimeSlotsForAllCourts();
    }

    private void initCities() {
        String[] cityNames = {"Саратов", "Москва", "Санкт-Петербург", "Казань"};
        for (String name : cityNames) {
            if (cityRepository.findByName(name).isEmpty()) {
                City city = new City();
                city.setName(name);
                city.setRegion(name.equals("Саратов") ? "Саратовская область" : name.equals("Москва") ? "Москва" : name.equals("Санкт-Петербург") ? "Ленинградская область" : "Республика Татарстан");
                cityRepository.save(city);
            }
        }
        log.info("Cities initialized: " + cityRepository.count());
    }

    private void initSportTypesAndCourts() {
        City saratov = cityRepository.findByName("Саратов").orElseThrow();

        SportType football = new SportType("Футбол", "Футбольные игры");
        SportType basketball = new SportType("Баскетбол", "Баскетбольные игры");
        SportType tennis = new SportType("Теннис", "Теннисные корты");
        sportTypeRepository.saveAll(Arrays.asList(football, basketball, tennis));

        SportCourt fc1 = new SportCourt("ФОК Звездный", "ул. Большая Затонская, 3", football, 30);
        SportCourt fc2 = new SportCourt("ФОК Юбилейный", "ул. Братьев Никитиных, 10", football, 30);
        SportCourt fc3 = new SportCourt("Площадка у СГЮА", "ул. Чернышевского 104", football, 30);
        SportCourt bc1 = new SportCourt("ФОК Звездный", "ул. Большая Затонская, 3", basketball, 20);
        SportCourt bc2 = new SportCourt("ФОК Юбилейный", "ул. Братьев Никитиных, 10", basketball, 20);
        SportCourt bc3 = new SportCourt("Площадка у СГЮА", "ул. Чернышевского 104", basketball, 20);
        SportCourt tc1 = new SportCourt("Ракета", "ул. Астраханская, 103", tennis, 4);
        SportCourt tc2 = new SportCourt("Первая школа тенниса", "ул. Чернышевского, 94", tennis, 2);

        List<SportCourt> courts = Arrays.asList(fc1, fc2, fc3, bc1, bc2, bc3, tc1, tc2);
        courts.forEach(c -> {
            c.setCity(saratov);
            c.setStatus("APPROVED");
            c.setOperatingHours("06:00–22:00");
            c.setWorkingDays("Ежедневно");
        });

        sportCourtRepository.saveAll(courts);
        log.info("Courts initialized: " + courts.size());
    }

    private void assignCityToCourtsMissingCity() {
        cityRepository.findByName("Саратов").ifPresent(saratov -> {
            List<SportCourt> noCityCourts = sportCourtRepository.findAll().stream()
                    .filter(c -> c.getCity() == null)
                    .collect(Collectors.toList());
            noCityCourts.forEach(c -> c.setCity(saratov));
            if (!noCityCourts.isEmpty()) {
                sportCourtRepository.saveAll(noCityCourts);
                log.info("Assigned Saratov city to " + noCityCourts.size() + " courts");
            }
        });
    }

    private void updateTimeSlotsForAllCourts() {
        List<SportCourt> allCourts = sportCourtRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDateTime cutoffTime = today.atStartOfDay(); // Удаляем только слоты прошлых дней

        log.info("=== UPDATING TIME SLOTS === Today: " + today + ", Courts: " + allCourts.size());

        // Удаляем бронирования прошедших слотов
        List<Booking> oldBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getCourtTimeSlot().getEndTime().isBefore(cutoffTime))
                .collect(Collectors.toList());
        bookingRepository.deleteAll(oldBookings);

        // Удаляем прошедшие слоты
        List<CourtTimeSlot> oldSlots = courtTimeSlotRepository.findAll().stream()
                .filter(s -> s.getEndTime().isBefore(cutoffTime))
                .collect(Collectors.toList());
        courtTimeSlotRepository.deleteAll(oldSlots);

        log.info("Cleaned up: " + oldBookings.size() + " bookings, " + oldSlots.size() + " slots");

        // Создаём слоты на сегодня, завтра, послезавтра (только если их нет)
        for (SportCourt court : allCourts) {
            for (int day = 0; day < 3; day++) {
                LocalDate date = today.plusDays(day);
                boolean hasSlots = courtTimeSlotRepository.findByCourtId(court.getId()).stream()
                        .anyMatch(s -> s.getStartTime().toLocalDate().equals(date));
                if (!hasSlots) {
                    addTimeSlotsForSingleDay(court, date);
                }
            }
        }

        log.info("Time slots updated successfully");
    }

    private void addTimeSlotsForSingleDay(SportCourt court, LocalDate date) {
        for (int hour = 6; hour < 22; hour++) {
            LocalDateTime start = LocalDateTime.of(date, LocalTime.of(hour, 0));
            courtTimeSlotRepository.save(new CourtTimeSlot(court, start, start.plusHours(1), 0));
        }
        log.info("Added slots for " + court.getName() + " on " + date);
    }
}
