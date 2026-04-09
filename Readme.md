# Email Scheduler

Spring Boot application for scheduling emails, storing jobs in PostgreSQL, and processing them in the background with a polling scheduler.

## What It Does

- Accepts email jobs through `POST /emails`
- Stores jobs in PostgreSQL with status `PENDING`
- Polls the database every 5 seconds for due jobs
- Sends emails using Spring Mail
- Retries failed jobs up to 3 times
- Marks jobs as `SENT` or `FAILED`

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Spring Mail
- Bean Validation
- Lombok
- MailHog for local email testing

## Flow

1. Client sends `POST /emails`
2. Request is validated
3. Job is saved in `email_jobs`
4. Scheduler checks every 5 seconds for due `PENDING` jobs
5. Email is sent through configured SMTP server
6. Job status is updated to `SENT` or `FAILED`

This project uses a polling-based background worker. It is not event-driven.

## Project Structure

```text
src/main/java/com/example/scheduler
|-- controller        # REST endpoints
|-- dto               # Request payloads
|-- entity            # JPA entities and enums
|-- repository        # Database access
|-- scheduler         # Background polling job
|-- service           # Business logic and mail sending
```

## Configuration

Secrets should not be committed to Git. This project now reads sensitive values from environment variables.

`src/main/resources/application.properties` uses placeholders like:

```properties
spring.datasource.password=${DB_PASSWORD:}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
```

You can provide values in any of these ways:

1. Environment variables
2. Run configuration variables in IntelliJ
3. Command-line `-D` / `--` properties
4. A local untracked Spring config file such as `application-local.properties`

### Recommended Environment Variables

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/emaildb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_db_password"
$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
$env:MAIL_USERNAME=""
$env:MAIL_PASSWORD=""
```

For a real SMTP server you would typically set:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="your_email"
$env:MAIL_PASSWORD="your_app_password"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS="true"
```

## Local Setup

### 1. Start PostgreSQL

Create a database named `emaildb`.

### 2. Start MailHog

```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

MailHog UI:

`http://localhost:8025`

### 3. Run the application

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Default application port:

`http://localhost:8089`

## API Endpoints

### Create Email Job

`POST /emails`

```json
{
  "email": "test@example.com",
  "subject": "Hello",
  "body": "Test email",
  "scheduledTime": "2026-04-09T18:30:00"
}
```

### Get All Jobs

`GET /emails`

### Get Job By Id

`GET /emails/{id}`

### Retry a Job

`POST /emails/{id}/retry`

Resets the job to `PENDING`, sets `retryCount` to `0`, and schedules it for immediate processing.

## Status Model

- `PENDING` - waiting to be processed
- `SENT` - sent successfully
- `FAILED` - failed after 3 attempts

## Notes

- If `scheduledTime` is in the past, the service changes it to the current time.
- The scheduler runs every 5 seconds using Spring `@Scheduled`.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development but is usually replaced with migrations in production.

## Safe GitHub Push Checklist

- Do not commit real passwords in `application.properties`
- Use environment variables for secrets
- Keep local-only config files untracked
- Rotate any secret that was previously committed, even if you delete it later

## Future Improvements

- Replace field injection with constructor injection
- Add exception logging and failure reason storage
- Add database migration tool such as Flyway
- Add Swagger/OpenAPI documentation
- Add tests for controller, service, and scheduler behavior


