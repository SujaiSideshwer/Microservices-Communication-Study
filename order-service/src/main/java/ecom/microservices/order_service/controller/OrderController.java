package ecom.microservices.order_service.controller;

import ecom.microservices.order_service.event.OrderEventType;
import ecom.microservices.order_service.event.PaymentCompletedEvent;
import ecom.microservices.order_service.service.CommunicationDispatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final CommunicationDispatcher dispatcher;

    public OrderController(CommunicationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Event (a) — PLACE ORDER.
     * Needs an immediate in-stock answer, so the dispatcher routes this to SYNCHRONOUS REST.
     */
    @PostMapping("/{productId}")
    public String placeOrder(@PathVariable Long productId) {
        return dispatcher.handle(OrderEventType.ORDER_PLACEMENT, productId);
    }

    /**
     * Event (b) — CONFIRM PAYMENT.
     * Fire-and-forget after payment succeeds, so the dispatcher routes this to ASYNCHRONOUS KAFKA.
     * Returns instantly; inventory-service reacts on its own thread.
     */
    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestBody PaymentCompletedEvent event) {
        return dispatcher.handle(OrderEventType.PAYMENT_CONFIRMATION, event);
    }
}
