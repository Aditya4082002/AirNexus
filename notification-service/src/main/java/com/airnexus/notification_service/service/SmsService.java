package com.airnexus.notification_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Async
    public void sendSms(String to, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS sent successfully to: {} with SID: {}", to, message.getSid());

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
        }
    }

    public void sendBookingConfirmationSms(String to, String pnr) {
        String message = String.format(
                "Your AirNexus booking is confirmed! PNR: %s. Check your email for details.",
                pnr
        );
        sendSms(to, message);
    }

    public void sendCheckInReminderSms(String to, String pnr) {
        String message = String.format(
                "Your flight departs in 24 hours! PNR: %s. Web check-in is now open. - AirNexus",
                pnr
        );
        sendSms(to, message);
    }
}
