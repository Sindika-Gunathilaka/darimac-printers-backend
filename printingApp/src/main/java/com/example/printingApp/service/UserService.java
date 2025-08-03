package com.example.printingApp.service;

import com.example.printingApp.model.User;
import com.example.printingApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get active users
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Get users by role
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRoleOrderByCreatedAtDesc(role);
    }

    // Search users by name
    public List<User> searchUsersByName(String name) {
        return userRepository.searchByName(name);
    }

    // Get users with active loans
    public List<User> getUsersWithActiveLoans() {
        return userRepository.findUsersWithActiveLoans();
    }

    // Check if username exists
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Check if email exists
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Create new user
    @Transactional
    public User createUser(User user) {
        // Validate username and email uniqueness
        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Update user
    @Transactional
    public User updateUser(Long id, User user) {
        return getUserById(id)
                .map(existingUser -> {
                    // Check username uniqueness (if changed)
                    if (!existingUser.getUsername().equals(user.getUsername()) &&
                            existsByUsername(user.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                    }

                    // Check email uniqueness (if changed)
                    if (!existingUser.getEmail().equals(user.getEmail()) &&
                            existsByEmail(user.getEmail())) {
                        throw new IllegalArgumentException("Email already exists");
                    }

                    user.setId(id);
                    user.setCreatedAt(existingUser.getCreatedAt());

                    // Keep existing password if not provided
                    if (user.getPassword() == null || user.getPassword().isEmpty()) {
                        user.setPassword(existingUser.getPassword());
                    } else {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Delete user
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Toggle user active status
    @Transactional
    public User toggleUserActive(Long id) {
        return getUserById(id)
                .map(user -> {
                    user.setIsActive(!user.getIsActive());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Update password
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        getUserById(id)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Authenticate user (simplified - in real app you'd use Spring Security)
    public User authenticateUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);


        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // ADD THESE DEBUG LINES
            System.out.println("=== DEBUG AUTHENTICATION ===");
            System.out.println("Input username/email: " + usernameOrEmail);
            System.out.println("Input password: " + password);
            System.out.println("Found user: " + user.getUsername());
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Password starts with $2a: " + (user.getPassword() != null && user.getPassword().startsWith("$2a")));
            System.out.println("Password length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
            System.out.println("Is active: " + user.getIsActive());
            System.out.println("===============================");


            // Check if user is active
            if (!user.getIsActive()) {
                throw new RuntimeException("User account is deactivated");
            }

            // Check password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update last login time
                user.setLastLoginAt(LocalDateTime.now());
                User savedUser = userRepository.save(user);

                // The password will be automatically excluded from JSON response due to @JsonProperty(access = WRITE_ONLY)
                return savedUser;
            } else {
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Get user profile (without sensitive information)
    public User getUserProfile(Long id) {
        return getUserById(id)
                .map(user -> {
                    // The password will be automatically excluded from JSON response due to @JsonProperty(access = WRITE_ONLY)
                    return user;
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}