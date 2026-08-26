package uk.bit1.resilience4jbulkheaddemo.rest;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;

    private String description;

    private BigDecimal amount;

    private String emailStatus;

    private Instant createdAt;

    protected Order() {
    }

    public Order(String customerEmail, String description, BigDecimal amount, String emailStatus, Instant createdAt) {
        this.customerEmail = customerEmail;
        this.description = description;
        this.amount = amount;
        this.emailStatus = emailStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
