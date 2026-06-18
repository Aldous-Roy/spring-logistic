package com.example.logistics.service;

import com.example.logistics.config.TwilioProperties;
import com.example.logistics.entity.DeliveryOrder;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);
    private final TwilioProperties properties;

    @PostConstruct
    public void init() {
        if (properties.accountSid() != null && !properties.accountSid().isBlank()
                && properties.authToken() != null && !properties.authToken().isBlank()) {
            Twilio.init(properties.accountSid(), properties.authToken());
        } else {
            log.warn("Twilio credentials not configured. SMS notifications will be skipped.");
        }
    }

    public void sendDriverNearbyAlert(DeliveryOrder order, String eta) {
        String msg = String.format("[Virtusa Logistics] Your delivery (%s) is arriving soon! Our driver is 2 stops away. Estimated arrival: %s. Please ensure someone is available.",
                order.getOrderId(), eta);
        sendSms(order.getCustomerPhone(), msg);
    }

    public void sendDeliveryCompletedAlert(DeliveryOrder order, String podUrl) {
        String time = order.getUpdatedAt().format(DateTimeFormatter.ofPattern("h:mm a"));
        String msg = String.format("[Virtusa Logistics] Your package (%s) has been delivered at %s to %s. Signed POD: %s",
                order.getOrderId(), time, order.getDeliveryAddress(), podUrl);
        sendSms(order.getCustomerPhone(), msg);
    }

    public void sendDeliveryFailedAlert(DeliveryOrder order, String reason, String rescheduleLink, String supportPhone) {
        String shortReason = reason != null && reason.length() > 50 ? reason.substring(0, 47) + "..." : (reason != null ? reason : "Delivery issue");
        String msg = String.format("[Virtusa Logistics] Delivery failed for order %s. Reason: %s. Please reschedule here: %s. Contact dispatcher: %s",
                order.getOrderId(), shortReason, rescheduleLink, supportPhone);
        sendSms(order.getCustomerPhone(), msg);
    }

    private void sendSms(String toPhone, String body) {
        if (properties.accountSid() == null || properties.accountSid().isBlank()
                || properties.phoneNumber() == null || properties.phoneNumber().isBlank()) {
            log.info("Mock SMS to {}: {}", toPhone, body);
            return;
        }
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(properties.phoneNumber()),
                    body
            ).create();
            log.info("Sent SMS to {}: {}", toPhone, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
        }
    }
}
