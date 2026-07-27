package ecom.microservices.inventory_service.event;

/**
 * Incoming message consumed from the "payment-completed" topic.
 * Field names must match what order-service publishes so JSON maps correctly
 * (the two services deliberately keep independent copies of the event shape).
 */
public record PaymentCompletedEvent(
        String orderId,
        Long productId,
        int quantity,
        double amount
) {
}
