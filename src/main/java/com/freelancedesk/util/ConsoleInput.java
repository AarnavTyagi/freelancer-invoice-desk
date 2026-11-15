package com.freelancedesk.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Reads typed console input and keeps asking until the value is valid. */
public class ConsoleInput {
    private final Scanner scanner = new Scanner(System.in);

    public String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    public BigDecimal readDecimal(String prompt) {
        while (true) {
            try {
                return new BigDecimal(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Enter a number, for example 1500 or 12.5.");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readText(prompt + " (yyyy-MM-dd): "));
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd.");
            }
        }
    }

    public boolean confirm(String prompt) {
        return readText(prompt + " (y/n): ").equalsIgnoreCase("y");
    }
}
