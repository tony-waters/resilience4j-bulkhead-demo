package uk.bit1.resilience4jbulkheaddemo.rest;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderService {

    private final OrderRepository orderRepository;
    private final EmailClient emailClient;

    OrderService(OrderRepository orderRepository, EmailClient emailClient) {
        this.orderRepository = orderRepository;
        this.emailClient = emailClient;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(
                request.customerEmail(),
                request.description(),
                request.amount(),
                "PENDING",
                Instant.now()));

        EmailDeliveryResult deliveryResult;
        try {
            BulkheadOrderContext.setOrderId(order.getId());
            deliveryResult = emailClient.sendOrderConfirmation(order);
        } finally {
            BulkheadOrderContext.clear();
        }
        order.setEmailStatus(deliveryResult.status());

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }
}
