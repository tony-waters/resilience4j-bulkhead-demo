package uk.bit1.resilience4jbulkheaddemo.rest;

final class BulkheadOrderContext {

    private static final ThreadLocal<Long> CURRENT_ORDER_ID = new ThreadLocal<>();

    private BulkheadOrderContext() {
    }

    static void setOrderId(Long orderId) {
        CURRENT_ORDER_ID.set(orderId);
    }

    static Long getOrderId() {
        return CURRENT_ORDER_ID.get();
    }

    static void clear() {
        CURRENT_ORDER_ID.remove();
    }
}
