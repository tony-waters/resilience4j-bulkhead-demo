package uk.bit1.resilience4jbulkheaddemo.rest;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class BulkheadEventLogger {

    private static final Logger log = LoggerFactory.getLogger(BulkheadEventLogger.class);

    private final BulkheadRegistry bulkheadRegistry;

    BulkheadEventLogger(BulkheadRegistry bulkheadRegistry) {
        this.bulkheadRegistry = bulkheadRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    void registerBulkheadLogging() {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("emailService");

        bulkhead.getEventPublisher()
                .onCallPermitted(event -> log.info(
                        "bulkhead permitted call orderId={} name={} availableConcurrentCalls={}",
                        BulkheadOrderContext.getOrderId(),
                        event.getBulkheadName(),
                        bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .onCallRejected(event -> log.warn(
                        "bulkhead rejected call orderId={} name={} availableConcurrentCalls={}",
                        BulkheadOrderContext.getOrderId(),
                        event.getBulkheadName(),
                        bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .onCallFinished(event -> log.info(
                        "bulkhead finished call orderId={} name={} availableConcurrentCalls={}",
                        BulkheadOrderContext.getOrderId(),
                        event.getBulkheadName(),
                        bulkhead.getMetrics().getAvailableConcurrentCalls()));
    }
}
