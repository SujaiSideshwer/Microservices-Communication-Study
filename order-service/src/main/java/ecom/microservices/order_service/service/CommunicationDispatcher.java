package ecom.microservices.order_service.service;

import ecom.microservices.order_service.event.CommunicationMode;
import ecom.microservices.order_service.event.OrderEventType;
import ecom.microservices.order_service.event.PaymentCompletedEvent;
import ecom.microservices.order_service.kafka.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The RUNTIME ROUTER.
 * Both communication styles coexist here. For each incoming event we look up the
 * event type, read the mode it is bound to, and delegate to the matching mechanism:
 *   ORDER_PLACEMENT      -> SYNCHRONOUS_REST   -> OrderService (RestClient call)
 *   PAYMENT_CONFIRMATION -> ASYNCHRONOUS_KAFKA -> OrderEventPublisher (Kafka send)
 */
@Service
public class CommunicationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommunicationDispatcher.class);

    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;

    public CommunicationDispatcher(OrderService orderService, OrderEventPublisher orderEventPublisher) {
        this.orderService = orderService;
        this.orderEventPublisher = orderEventPublisher;
    }

    /** Event (a): order placement — routed to synchronous REST. */
    public String handle(OrderEventType eventType, Long productId) {
        CommunicationMode mode = resolveMode(eventType);
        return switch (mode) {
            case SYNCHRONOUS_REST -> orderService.placeOrder(productId);
            case ASYNCHRONOUS_KAFKA ->
                    throw new IllegalArgumentException(eventType + " does not carry enough data for the Kafka path");
        };
    }

    /** Event (b): payment confirmation — routed to asynchronous Kafka. */
    public String handle(OrderEventType eventType, PaymentCompletedEvent event) {
        CommunicationMode mode = resolveMode(eventType);
        return switch (mode) {
            case ASYNCHRONOUS_KAFKA -> {
                orderEventPublisher.publishPaymentCompleted(event);
                yield "payment confirmed for order " + event.orderId()
                        + " — inventory will be updated asynchronously";
            }
            case SYNCHRONOUS_REST ->
                    throw new IllegalArgumentException(eventType + " is not a REST-style event");
        };
    }

    private CommunicationMode resolveMode(OrderEventType eventType) {
        CommunicationMode mode = eventType.mode();
        log.info("[DISPATCH] event={} -> mode={}", eventType, mode);
        return mode;
    }
}
