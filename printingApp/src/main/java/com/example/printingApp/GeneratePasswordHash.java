package com.example.printingApp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "myLovingDhanu@2025";

        // Generate 3 different hashes to show they're all valid
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(plainPassword);
            System.out.println("Hash " + i + ": " + hash);
            System.out.println("Length: " + hash.length());
            System.out.println("Starts with $2a: " + hash.startsWith("$2a"));
            System.out.println("Verification test: " + encoder.matches(plainPassword, hash));
            System.out.println("---");
        }

        // Generate one final hash for database update
        String finalHash = encoder.encode(plainPassword);
        System.out.println("Use this hash in your database:");
        System.out.println(finalHash);
        System.out.println("\nSQL Command:");
        System.out.println("UPDATE users SET password = '" + finalHash + "' WHERE email = 'dhanushka@darimac.com';");
    }
}
