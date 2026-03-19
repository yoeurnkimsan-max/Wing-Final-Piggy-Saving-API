package com.example.piggy_saving.util;

import java.util.Random;

public class AccountNumberGenerator {

    private static final Random RANDOM = new Random();

    /**
     * Generate a unique account number.
     * Format: [branchCode][6-digit random][5-digit random]
     * Example: 1 234567 89012
     *
     * @param branchCode the branch code (static 1 for minimal project)
     * @return generated account number as string
     */
    public static String generateAccountNumber(int branchCode) {
        // 6-digit random part
        int midPart = 100000 + RANDOM.nextInt(900000);

        // 5-digit random part
        int lastPart = 10000 + RANDOM.nextInt(90000);

        // Combine all parts
        return String.format("%d%06d%05d", branchCode, midPart, lastPart);
    }

    // Optional: Overload for static branch code 1
    public static String generateAccountNumber() {
        return generateAccountNumber(1);
    }
}