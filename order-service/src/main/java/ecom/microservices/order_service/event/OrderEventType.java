package ecom.microservices.order_service.event;

/**
 * Business events in the order lifecycle, each bound to the communication mode
 * that best fits it. This binding is what lets the dispatcher choose REST vs Kafka
 * at runtime purely from the event type.
 */
public enum OrderEventType {

    // (a) Placing an order needs an IMMEDIATE stock answer -> synchronous REST.
    ORDER_PLACEMENT(CommunicationMode.SYNCHRONOUS_REST),

    // (b) Confirming after payment is fire-and-forget -> asynchronous Kafka.
    PAYMENT_CONFIRMATION(CommunicationMode.ASYNCHRONOUS_KAFKA);

    private final CommunicationMode mode;

    OrderEventType(CommunicationMode mode) {
        this.mode = mode;
    }

    public CommunicationMode mode() {
        return mode;
    }
}
