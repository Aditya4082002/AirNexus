package com.airnexus.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendBookingConfirmation(String to, String pnr, String flightDetails) {
        String subject = "Booking Confirmed - PNR: " + pnr;
        String body = String.format("""
            Dear Passenger,
            
            Your booking has been confirmed successfully!
            
            PNR: %s
            Flight Details: %s
            
            Please check in 24 hours before departure.
            
            Thank you for choosing AirNexus!
            
            Best regards,
            AirNexus Team
            """, pnr, flightDetails);

        sendEmail(to, subject, body);
    }

    public void sendCheckInReminder(String to, String pnr, String flightNumber) {
        String subject = "Check-in Reminder - " + flightNumber;
        String body = String.format("""
            Dear Passenger,
            
            Your flight %s departs in 24 hours!
            
            PNR: %s
            
            Web check-in is now open. Please check in to avoid last-minute hassles.
            
            Safe travels!
            
            Best regards,
            AirNexus Team
            """, flightNumber, pnr);

        sendEmail(to, subject, body);
    }
}
