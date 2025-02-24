package com.example.soloTest.controller;

import com.example.soloTest.dto.request.LoginRequest;
import com.example.soloTest.dto.request.UserRequest;
import com.example.soloTest.dto.response.ApiResponse;
import com.example.soloTest.dto.response.PaginatedResponse;
import com.example.soloTest.dto.response.UserResponse;
import com.example.soloTest.exception.DataNotFoundException;
import com.example.soloTest.exception.DuplicateDataException;
import com.example.soloTest.repository.UserRepository;
import com.example.soloTest.service.UserService;
import com.example.soloTest.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    // register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest){
        try {
            UserResponse userResponse = userService.registerUser(userRequest);
            return ResponseEntity.ok(new ApiResponse<>(200, userResponse));
        } catch (DuplicateDataException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, e.getMessage()));
        } catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, e.getMessage()));
        }
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        try {
            UserResponse userResponse = userService.loginUser(loginRequest);
            String token = jwtUtil.generateToken(userResponse.getUsername());
            return ResponseEntity.ok(new ApiResponse<String>(200, token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<String>(401, "invalid username or password"));
        }
    }

    // get user by username
    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username){
        try{
            UserResponse userResponse = userService.getUserByUsername(username);
            return ResponseEntity.ok().body(new ApiResponse<>(200, userResponse));
        } catch (Exception e){
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage()));
        }
    }

    // get all users
    @GetMapping
    public ResponseEntity<?> getAllUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Page<?> users = userService.findAll(page, size);
            return ResponseEntity
                    .ok(new PaginatedResponse<>(200, users));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") UUID userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity
                    .ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully"));
        } catch (DataNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete user: "+ e.getMessage()));
        }
    }

    // update user
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable UUID userId, @RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = userService.updateUser(userId, userRequest);
            return ResponseEntity.ok().body(new ApiResponse<>(200, userResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "User not found"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, e.getMessage()));
        }
    }

}
