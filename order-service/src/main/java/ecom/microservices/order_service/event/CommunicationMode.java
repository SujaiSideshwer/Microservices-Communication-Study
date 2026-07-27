package ecom.microservices.order_service.event;

/**
 * The two ways our microservices talk to each other.
 * The dispatcher picks one of these at runtime depending on the event.
 */
public enum CommunicationMode {
    SYNCHRONOUS_REST,     // caller blocks and waits for an immediate answer (RestClient)
    ASYNCHRONOUS_KAFKA    // caller fires an event and moves on (Kafka publish/subscribe)
}
