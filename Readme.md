# Email Scheduler

Spring Boot application for asynchronous email processing using RabbitMQ, PostgreSQL, and Spring Mail. Email requests are published as messages, processed by a consumer, and failed messages are routed to a dead-letter queue.

## What It Does

- Accepts email requests through `POST /emails`
- Publishes email payloads to RabbitMQ
- Consumes messages from a RabbitMQ queue
- Sends emails through Spring Mail
- Retries failed listener executions
- Routes exhausted failures to a dead-letter queue
- Persists failed jobs in PostgreSQL from the DLQ consumer

## Architecture

This version is event-driven.

High-level flow:

1. Client sends `POST /emails`
2. Controller validates the request body
3. Service builds an `EmailMessage`
4. Service publishes the message to RabbitMQ exchange `email_exchange`
5. RabbitMQ routes it to `email_queue`
6. `EmailConsumer` listens on `email_queue`
7. Consumer tries to send the email
8. If sending fails, the listener throws an exception
9. RabbitMQ listener retries are attempted
10. If retries are exhausted, the message is dead-lettered to `email_dlq`
11. `EmailDLQConsumer` listens on `email_dlq` and stores the failed job in PostgreSQL with status `FAILED`

This is different from the earlier polling model:

- there is no active database polling loop driving delivery
- job processing is triggered by message arrival
- RabbitMQ is now the transport layer between producer and consumer

## RabbitMQ Topology

Configured in `RabbitMQConfig`:

- Main exchange: `email_exchange`
- Main queue: `email_queue`
- Routing key: `email_routing`
- Dead-letter exchange: `email_dlx`
- Dead-letter queue: `email_dlq`

The main queue is configured with a dead-letter exchange:

- `x-dead-letter-exchange = email_dlx`

When a message cannot be processed successfully after retries, RabbitMQ moves it to the dead-letter exchange, and then it is routed to the dead-letter queue.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring AMQP
- RabbitMQ
- PostgreSQL
- Spring Mail
- Bean Validation
- Lombok
- MailHog for local email testing

## Project Structure

```text
src/main/java/com/example/scheduler
|-- config           # RabbitMQ exchanges, queues, bindings, and message converter
|-- consumer         # Main queue and DLQ listeners
|-- controller       # REST endpoints
|-- dto              # API request DTO and RabbitMQ message payload
|-- entity           # JPA entity and status enum
|-- repository       # Database access
|-- service          # Message publishing and email sending
|-- scheduler        # Legacy polling-based scheduler from the old design
```

## Configuration

Secrets should not be committed to Git. The project loads local secrets from `application-secrets.properties` and uses externalized values for database and mail credentials.

Example local secret values:

```properties
DB_URL=jdbc:postgresql://localhost:5432/emaildb
DB_USERNAME=postgres
DB_PASSWORD=your_db_password

MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
```

Current RabbitMQ configuration:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.multiplier=2.0
spring.rabbitmq.listener.simple.retry.max-interval=5000
```

Retry behavior:

- listener retry is enabled
- max attempts is `3`
- first retry delay is `1 second`
- delay multiplies by `2.0`
- max delay is `5 seconds`

## Local Setup

### 1. Start PostgreSQL

Create a database named `emaildb`.

### 2. Start RabbitMQ

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

RabbitMQ management UI:

`http://localhost:15672`

Default credentials:

- username: `guest`
- password: `guest`

### 3. Start MailHog

```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

MailHog UI:

`http://localhost:8025`

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Application URL:

`http://localhost:8089`

## API Endpoints

### Publish Email Job

`POST /emails`

Publishes a message to RabbitMQ.

```json
{
  "email": "test@example.com",
  "subject": "Hello",
  "body": "Test email",
  "scheduledTime": "2026-04-11T18:30:00"
}
```

### Get Persisted Jobs

`GET /emails`

Returns jobs stored in PostgreSQL.

### Get Persisted Job By Id

`GET /emails/{id}`

Returns a single stored job from PostgreSQL.

### Retry Stored Job

`POST /emails/{id}/retry`

Resets the stored record to `PENDING`, sets `retryCount` to `0`, and updates `scheduledTime` to now.

## Current Persistence Behavior

This part is important because it is different from the older version:

- `POST /emails` does not save a database row first
- the main path publishes directly to RabbitMQ
- failed messages are persisted by `EmailDLQConsumer`
- successful sends are not currently stored in PostgreSQL

So PostgreSQL is currently acting as a failure record store, not the primary queue.

## Dead Letter Queue

Why the dead-letter queue exists:

- prevents failed messages from looping forever in the main queue
- separates poison messages from normal traffic
- gives you a place to inspect operational failures
- allows later replay or manual recovery workflows

In this project, a message reaches the DLQ when:

1. `EmailConsumer` receives a message
2. email sending throws an exception
3. listener retries are exhausted
4. RabbitMQ dead-letters the message
5. `EmailDLQConsumer` consumes it from `email_dlq`
6. the failed email is stored in the database with status `FAILED`

## Important Notes

- `scheduledTime` is part of the payload, but the consumer currently sends the email immediately when the message is consumed.
- The `retry` endpoint currently updates the database record only. It does not republish that job back to RabbitMQ.
- The project still contains the old polling scheduler class, but the current delivery model is RabbitMQ-driven.
- `spring.jpa.hibernate.ddl-auto=update` is useful for development, but migrations are better for production.

## Safe GitHub Push Checklist

- do not commit real credentials
- keep `application-secrets.properties` untracked
- review `application.properties` before push
- rotate any credential that was committed in the past

## Future Improvements

- republish failed jobs to RabbitMQ from the retry endpoint
- persist successful deliveries as well as failures
- store error messages and retry metadata in the database
- remove legacy polling code if it is no longer needed
- move schema management to Flyway or Liquibase
- switch field injection to constructor injection
