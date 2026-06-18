package com.example.logistics.service;

import com.example.logistics.entity.DeliveryOrder;
import com.example.logistics.entity.DriverLocation;
import com.example.logistics.entity.DriverProfile;
import com.example.logistics.entity.enums.DeliveryPriority;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    public void notifyHighPriorityFail(DeliveryOrder order) {
        if (order.getPriority() != DeliveryPriority.HIGH && order.getPriority() != DeliveryPriority.URGENT) {
            return;
        }
        String title = "🚨 " + order.getPriority() + " PRIORITY DELIVERY FAILED";
        String body = String.format("Order %s failed at %s. Reason: %s. Requires immediate dispatcher attention.",
                order.getOrderId(), order.getDeliveryAddress(), order.getFailedReasonNotes());
        
        sendToTopic("dispatchers", title, body);
    }

    public void notifyDriverOffline(DriverProfile driver, DriverLocation lastLocation) {
        String title = "⚠️ Driver Offline: " + driver.getFirstName() + " " + driver.getLastName();
        String body = String.format("Driver %s has been offline for > 5 minutes. Last known location: %.4f, %.4f",
                driver.getEmployeeId(), lastLocation.getLatitude(), lastLocation.getLongitude());
        
        sendToTopic("dispatchers", title, body);
    }

    private void sendToTopic(String topic, String title, String body) {
        try {
            // This will throw if FirebaseApp is not initialized (which it isn't yet, as per plan)
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic(topic)
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message to topic {}: {}", topic, response);
        } catch (Exception e) {
            log.warn("Failed to send FCM push notification (Firebase likely not configured): {}", e.getMessage());
        }
    }
}
