package com.sportbuddy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsService {

    @Value("${sms.api-id}")
    private String apiId;

    @Autowired
    private RestTemplate restTemplate;

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public void sendSms(String phone) {
        // 1. Очищаем от мусора
        String cleanPhone = phone.replaceAll("[^0-9]", "");

        // 2. Превращаем 89... в 79...
        if (cleanPhone.startsWith("8") && cleanPhone.length() == 11) {
            cleanPhone = "7" + cleanPhone.substring(1);
        }
        // Если номер уже 79..., оставляем как есть

        // 3. Генерируем код
        String code = String.valueOf(new Random().nextInt(9000) + 1000);
        otpStorage.put(cleanPhone, code); // В память сохраняем БЕЗ плюса (для удобства поиска)

        System.out.println("\n🔐 КОД (Резерв): " + code + "\n");

        try {
            // ВАЖНО: Добавляем %2B перед номером. Это код символа "+"
            // Получится: to=%2B79173107021 (то есть +79173107021)
            String url = String.format(
                    "https://sms.ru/sms/send?api_id=%s&to=%%2B%s&msg=%s&json=1",
                    apiId, cleanPhone, "SportBuddy: " + code
            );

            // Диагностика (скопируй эту ссылку в браузер, если снова не сработает)
            System.out.println("🔗 Ссылка: " + url);

            String response = restTemplate.getForObject(url, String.class);
            System.out.println("✅ Ответ SMS.RU: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifyCode(String phone, String code) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.startsWith("8") && cleanPhone.length() == 11) {
            cleanPhone = "7" + cleanPhone.substring(1);
        }

        String savedCode = otpStorage.get(cleanPhone);
        if (savedCode != null && savedCode.equals(code)) {
            otpStorage.remove(cleanPhone);
            return true;
        }
        return false;
    }
}
