package com.lms.payment.event;

import com.lms.payment.model.Order;
import com.lms.payment.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Emitted after successful Razorpay webhook verification.
     * Consumed by enrollment-service → auto-enroll student.
     */
    public void publishPaymentSuccess(Order order, String razorpayPaymentId) {
        for (OrderItem item : order.getItems()) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("orderId", order.getId());
            event.put("orderNumber", order.getOrderNumber());
            event.put("userId", order.getUserId());
            event.put("courseId", item.getCourseId());
            event.put("trackId", item.getTrackId());
            event.put("amount", order.getTotal());
            event.put("razorpayPaymentId", razorpayPaymentId);

            kafkaTemplate.send("payment.success", String.valueOf(order.getUserId()), event);
            log.info("Published payment.success: orderId={} userId={} courseId={}",
                    order.getId(), order.getUserId(), item.getCourseId());
        }
    }

    public void publishPaymentFailed(Order order, String reason) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("orderId", order.getId());
        event.put("orderNumber", order.getOrderNumber());
        event.put("userId", order.getUserId());
        event.put("amount", order.getTotal());
        event.put("reason", reason);

        kafkaTemplate.send("payment.failed", String.valueOf(order.getUserId()), event);
        log.info("Published payment.failed: orderId={} reason={}", order.getId(), reason);
    }
}
