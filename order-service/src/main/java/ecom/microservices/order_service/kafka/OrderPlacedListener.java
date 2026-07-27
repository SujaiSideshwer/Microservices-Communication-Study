package ecom.microservices.order_service.kafka;

import ecom.microservices.order_service.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBER side (return leg of the loop).
 * inventory-service publishes to "order-placed" after it reserves stock;
 * order-service consumes it here to confirm the order lifecycle completed.
 * This method runs on a Kafka listener thread, fully decoupled from the HTTP request.
 */
@Component
public class OrderPlacedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

    @KafkaListener(topics = "${app.kafka.topic.order-placed}", groupId = "order-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("[KAFKA][ASYNC] order-service received OrderPlacedEvent: order {} -> {}",
                event.orderId(), event.status());
    }
}
