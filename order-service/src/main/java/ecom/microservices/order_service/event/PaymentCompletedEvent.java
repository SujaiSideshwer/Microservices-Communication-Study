package ecom.microservices.order_service.event;

/**
 * Message published to the "payment-completed" topic once a customer's payment succeeds.
 * inventory-service subscribes to this to finalize the order asynchronously.
 */
public record PaymentCompletedEvent(
        String orderId,
        Long productId,
        int quantity,
        double amount
) {
}
