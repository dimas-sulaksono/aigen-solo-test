package com.example.soloTest.controller;

import com.example.soloTest.dto.response.ApiResponse;
import com.example.soloTest.dto.response.UserResponse;
import com.example.soloTest.repository.UserRepository;
import com.example.soloTest.service.UserService;
import com.example.soloTest.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username){
        try{
            UserResponse userResponse = userService.getUserByUsername(username);
            return ResponseEntity.ok().body(new ApiResponse<>(200, userResponse));
        } catch (Exception e){
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage()));
        }
    }

}
