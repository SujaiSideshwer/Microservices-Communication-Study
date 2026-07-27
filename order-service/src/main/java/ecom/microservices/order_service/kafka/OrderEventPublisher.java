package ecom.microservices.order_service.kafka;

import ecom.microservices.order_service.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * PUBLISHER side of the async flow.
 * Sends a PaymentCompletedEvent to Kafka. The call returns immediately (fire-and-forget):
 * order-service does NOT wait for inventory-service — that is the whole point of async comm.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);


    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.payment-completed}")
    private String paymentCompletedTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        // Using orderId as the message key keeps all events for one order on the same partition (ordering guarantee).
        kafkaTemplate.send(paymentCompletedTopic, event.orderId(), event);
        log.info("[KAFKA][ASYNC] Published PaymentCompletedEvent to '{}': {}", paymentCompletedTopic, event);
    }
}
