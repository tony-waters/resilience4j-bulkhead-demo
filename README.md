# Resilience4j Bulkhead Demo

Two Spring Boot applications in one Maven repo:

- `rest-service`: REST + JPA order API backed by Postgres. It calls the downstream email API through a Resilience4j bulkhead.
- `email-service`: intentionally slow downstream service used to keep concurrent calls in flight.

The bulkhead is configured with `max-concurrent-calls: 2` and `max-wait-duration: 0`. When more than two order requests try to send email at the same time, Resilience4j rejects the overflow immediately and the REST service falls back to `EMAIL_DEFERRED`. Orders are still saved.

> The project name intentionally uses `resilience4j-bulkhead-demo` to match the requested spelling.

## Build

```bash
./mvnw test
```

If the Maven wrapper is unavailable, run Maven directly:

```bash
mvn test
```

## Run

Run the full demo stack with Docker Compose:

```bash
docker compose up --build
```

Useful URLs:

- REST service: http://localhost:8081
- Email service: http://localhost:8082
- Postgres: `localhost:5432`, database `orders`, user `demo`, password `demo`
- REST health: http://localhost:8081/actuator/health
- Bulkhead actuator endpoint: http://localhost:8081/actuator/bulkheads

To run the services directly from Maven, start Postgres first:

```bash
docker compose up postgres
```

Start the email service:

```bash
mvn -pl email-service spring-boot:run
```

Start the REST service in another terminal:

```bash
mvn -pl rest-service spring-boot:run
```

## Try It

Create a single order:

```bash
curl -i -X POST http://localhost:8081/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerEmail":"alice@example.com","description":"Demo order","amount":42.50}'
```

List saved orders:

```bash
curl -i http://localhost:8081/api/orders
```

Run the k6 bulkhead test to create enough concurrent order traffic to saturate the email bulkhead:

```bash
docker compose --profile test run --rm k6-bulkhead
```

Watch the REST service logs in another terminal:

```bash
docker compose logs -f rest-service
```

The k6 run should report both `sent_email_responses` and `bulkhead_rejected_email_responses` above zero. The REST logs should include:

```text
bulkhead permitted call name=emailService
bulkhead rejected call name=emailService
bulkhead finished call name=emailService
```

The response body for rejected email work still has HTTP `201 Created`, because the order is saved and only the downstream email work is deferred:

```json
{
  "emailStatus": "EMAIL_DEFERRED"
}
```

## Why This Shows Bulkhead Behavior

`email-service` sleeps for 1 second on every request. While two email calls are sleeping, the `emailService` bulkhead has no available concurrent slots. Because `max-wait-duration` is zero, additional calls are rejected immediately instead of waiting for a slot. The fallback records the email as deferred while letting the order workflow complete.
