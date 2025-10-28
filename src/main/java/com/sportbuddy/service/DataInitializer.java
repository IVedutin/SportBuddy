package com.sportbuddy.service;

import com.sportbuddy.entity.SportType;
import com.sportbuddy.entity.SportCourt;
import com.sportbuddy.entity.CourtTimeSlot;
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

@Service
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportCourtRepository sportCourtRepository;

    @Autowired
    private CourtTimeSlotRepository courtTimeSlotRepository;

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

            // Добавляем БЕСПЛАТНЫЕ временные слоты с 6 утра до 22 вечера
            addFreeTimeSlotsForCourt(footballCourt1);
            addFreeTimeSlotsForCourt(footballCourt2);
            addFreeTimeSlotsForCourt(footballCourt3);
            addFreeTimeSlotsForCourt(basketballCourt1);
            addFreeTimeSlotsForCourt(basketballCourt2);
            addFreeTimeSlotsForCourt(basketballCourt3);
            addFreeTimeSlotsForCourt(tennisCourt1);
            addFreeTimeSlotsForCourt(tennisCourt2);
        }
    }

    private void addFreeTimeSlotsForCourt(SportCourt court) {
        LocalDate today = LocalDate.now();

        // Создаем слоты с 6 утра до 22 вечера с интервалом в 1 час
        for (int day = 0; day < 3; day++) { // на сегодня, завтра, послезавтра
            LocalDate currentDate = today.plusDays(day);

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
    }
}