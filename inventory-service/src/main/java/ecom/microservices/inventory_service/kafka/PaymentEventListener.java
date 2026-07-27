package ecom.microservices.inventory_service.kafka;

import ecom.microservices.inventory_service.event.OrderPlacedEvent;
import ecom.microservices.inventory_service.event.PaymentCompletedEvent;
import ecom.microservices.inventory_service.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBER side of the async flow.
 * Consumes PaymentCompletedEvent from the "payment-completed" topic on a Kafka
 * listener thread (fully decoupled from order-service), reserves stock, then
 * PUBLISHES an OrderPlacedEvent to the "order-placed" topic to stream the result.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.order-placed}")
    private String orderPlacedTopic;

    public PaymentEventListener(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topic.payment-completed}", groupId = "inventory-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[KAFKA][ASYNC] inventory-service consumed PaymentCompletedEvent: {}", event);

        // 1) Do the actual inventory work asynchronously.
        String status = inventoryService.reserveStock(event.productId(), event.quantity());

        // 2) Stream the outcome onto a separate topic.
        OrderPlacedEvent placed = new OrderPlacedEvent(event.orderId(), event.productId(), status);
        kafkaTemplate.send(orderPlacedTopic, placed.orderId(), placed);
        log.info("[KAFKA][ASYNC] Published OrderPlacedEvent to '{}': {}", orderPlacedTopic, placed);
    }
}
