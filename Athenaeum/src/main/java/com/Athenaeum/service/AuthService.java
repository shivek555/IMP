package com.Athenaeum.service;

import com.Athenaeum.dto.LoginRequest;
import com.Athenaeum.dto.LoginResponse;
import com.Athenaeum.entity.User;
import com.Athenaeum.repository.UserRepository;
import com.Athenaeum.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                return new LoginResponse(token, user.getUsername(), user.getEmail(), true);
            }
        }

        return new LoginResponse(null, null, null, false);
    }

    public User register(String username, String email, String password, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validateToken(String token, String username) {
        return jwtUtil.validateToken(token, username);
    }
}