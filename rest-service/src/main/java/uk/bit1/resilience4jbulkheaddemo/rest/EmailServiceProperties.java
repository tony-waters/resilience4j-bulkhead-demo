package uk.bit1.resilience4jbulkheaddemo.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.email-service")
public record EmailServiceProperties(String baseUrl) {
}
