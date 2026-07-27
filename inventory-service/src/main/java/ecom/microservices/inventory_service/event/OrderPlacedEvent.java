package ecom.microservices.inventory_service.event;

/**
 * Outgoing message published to the "order-placed" topic after stock is reserved.
 * order-service subscribes to it to confirm the order lifecycle finished.
 */
public record OrderPlacedEvent(
        String orderId,
        Long productId,
        String status
) {
}
