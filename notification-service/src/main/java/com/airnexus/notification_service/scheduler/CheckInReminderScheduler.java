package com.airnexus.notification_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckInReminderScheduler {

    // This would need Feign clients to Booking and Flight services
    // For now, it's a placeholder

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void sendCheckInReminders() {
        log.info("Running check-in reminder scheduler...");

        // TODO:
        // 1. Get all bookings with departure in next 24-25 hours
        // 2. Filter bookings not yet checked in
        // 3. Send check-in reminders via NotificationService

        log.info("Check-in reminder scheduler completed");
    }
}