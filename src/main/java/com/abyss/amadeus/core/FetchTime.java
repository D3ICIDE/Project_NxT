package com.abyss.amadeus.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FetchTime {
    public static String getCurrentDate() {
        // Formats the date like: "2026-06-18"
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDateTime.now().format(dateFormatter);
    }

    public static String getCurrentTime() {
        // Formats the time like: "10:14 PM"
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalDateTime.now().format(timeFormatter);
    }

}
