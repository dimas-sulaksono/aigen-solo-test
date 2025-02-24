package com.example.soloTest.service;

import com.example.soloTest.dto.request.LoginRequest;
import com.example.soloTest.dto.request.UserRequest;
import com.example.soloTest.dto.response.UserResponse;
import com.example.soloTest.exception.DataNotFoundException;
import com.example.soloTest.exception.DuplicateDataException;
import com.example.soloTest.model.User;
import com.example.soloTest.repository.UserRepository;
import com.example.soloTest.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with username: "+ username));
        return new CustomUserDetails(user);
    }

    // register
    @Transactional
    public UserResponse registerUser(UserRequest userRequest){
        Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new DuplicateDataException("User already exists with username: " + userRequest.getUsername());}

        Optional<User> existingEmail = userRepository.findByEmail(userRequest.getEmail());
        if (existingEmail.isPresent()) {
            throw new DuplicateDataException("Email already registered: " + userRequest.getEmail());}

        if (userRequest.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");}

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Optional.ofNullable(userRequest.getRole()).orElse("CUSTOMER"));
        user = userRepository.save(user);
        User register = userRepository.save(user);
        return convertToResponse(register);
    }

    // login
    public UserResponse loginUser(LoginRequest loginRequest){
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        if (userOptional.isEmpty()){
            throw new RuntimeException("User not found with username: "+ loginRequest.getUsername());}
        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");}
        return convertToResponse(user);
    }

    // cari user berdasarkan username
    public UserResponse getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return convertToResponse(user.get());
        } else {
            throw new RuntimeException("User not found with username: " + username);
        }
    }

    // get all users
    public Page<UserResponse> findAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userRepository.findAllByOrderByUsernameAsc(pageable);
            return users.map(this::convertToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all users: " + e.getMessage(), e);
        }
    }

    // update user
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest userRequest) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        User user = userOptional.get();
        if (userRequest.getUsername() != null) {
            user.setUsername(userRequest.getUsername());
        }
        if (userRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getRole() != null) {
            user.setRole(userRequest.getRole());
        }
        user = userRepository.save(user);

        return convertToResponse(user);
    }

    // delete user
    @Transactional
    public void deleteUser(UUID userId) {
        try {
            if(!userRepository.existsById(userId)) {
                throw new DataNotFoundException("Todolist with id " + userId + " not found");
            }
            userRepository.deleteById(userId);
        } catch (DataNotFoundException e){
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("Failed to delete todolist" + e.getMessage(), e);
        }
    }

    // convert to response
    private UserResponse convertToResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setUuid(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }

}
