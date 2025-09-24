package com.sportbuddy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Привет! Sportbuddy backend готов!";
    }
    @GetMapping("/greet")
    public String hiName(@RequestParam(value = "name", defaultValue = "Друг") String name){
        return "Привет, " + name+"!";
    }
}