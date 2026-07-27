package ecom.microservices.order_service.event;

/**
 * Message published to the "order-placed" topic by inventory-service after it has
 * consumed a payment event and reserved the stock. order-service subscribes to it
 * so it can log/track that the order was finalized — closing the async loop.
 */
public record OrderPlacedEvent(
        String orderId,
        Long productId,
        String status
) {
}
