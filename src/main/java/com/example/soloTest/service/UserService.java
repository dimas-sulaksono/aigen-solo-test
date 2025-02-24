package com.example.soloTest.service;

import com.example.soloTest.dto.response.UserResponse;
import com.example.soloTest.model.User;
import com.example.soloTest.repository.UserRepository;
import com.example.soloTest.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public UserResponse getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return convertToResponse(user.get());
        } else {
            throw new RuntimeException("User not found with username: " + username);
        }
    }

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
