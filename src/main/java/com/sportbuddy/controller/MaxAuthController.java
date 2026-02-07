package com.sportbuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class MaxAuthController {

    @GetMapping("/auth/max")
    public String redirectToMax() {
        String firstName = URLEncoder.encode("Илья", StandardCharsets.UTF_8);
        String lastName = URLEncoder.encode("Ведутин", StandardCharsets.UTF_8);
        return "redirect:/auth/max/callback?firstName=" + firstName + "&lastName=" + lastName;
    }

    @GetMapping("/auth/max/callback")
    public String maxCallback(String firstName, String lastName, Model model) {
        model.addAttribute("fullName", firstName + " " + lastName);
        return "greetings";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}