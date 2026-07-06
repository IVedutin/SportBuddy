package com.sportbuddy.controller;

import com.sportbuddy.entity.User;
import com.sportbuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    Map<String, Object> resp = new java.util.HashMap<>();
                    resp.put("id", user.getId());
                    resp.put("email", user.getEmail());
                    resp.put("firstName", user.getFirstName());
                    resp.put("lastName", user.getLastName());
                    resp.put("role", user.getRole() != null ? user.getRole().name() : "USER");
                    return ResponseEntity.ok().body(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return userRepository.findByEmail(auth.getName())
                .map(user -> {
                    Map<String, Object> resp = new java.util.HashMap<>();
                    resp.put("id", user.getId());
                    resp.put("email", user.getEmail());
                    resp.put("firstName", user.getFirstName());
                    resp.put("lastName", user.getLastName());
                    resp.put("role", user.getRole() != null ? user.getRole().name() : "USER");
                    return ResponseEntity.ok().body(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}