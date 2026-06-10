# Logistics Backend

Spring Boot 3.5.x backend for logistics management.

## Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- WebSocket
- PostgreSQL
- JWT
- Lombok

## Run

Set these environment variables before starting the app:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION_MS`

Start the application:

```bash
./mvnw spring-boot:run
```

## Docker

Build the image:

```bash
docker build -t virtusa-logistics .
```

Run it locally with a Supabase PostgreSQL connection:

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=<supabase-password> \
  -e APP_JWT_SECRET=change-me \
  -e APP_JWT_EXPIRATION_MS=86400000 \
  virtusa-logistics
```

On Render, use the same env vars and point `SPRING_DATASOURCE_URL` at Supabase. The database stays in Supabase, so container restarts do not wipe your data. POD images are now stored in PostgreSQL as binary data, not on the container filesystem.

Run tests:

```bash
./mvnw test
```

## API Examples

### Signup

`POST /api/auth/signup`

```json
{
  "employeeId": "EMP1001",
  "name": "Alice Dispatcher",
  "password": "Password123!",
  "role": "DISPATCHER"
}
```

Success response:

```json
{
  "status": "success",
  "statusCode": 200,
  "data": {
    "token": "jwt-token",
    "tokenType": "Bearer",
    "expiresInMs": 86400000,
    "employeeId": "EMP1001",
    "name": "Alice Dispatcher",
    "role": "DISPATCHER"
  }
}
```

### Create Route

`POST /api/routes`

```json
{
  "routeCode": "RT-20260610-01",
  "routeDate": "2026-06-10",
  "totalDistanceKm": 42.5,
  "estimatedDurationMins": 120,
  "routePolyline": "encoded-polyline"
}
```

### Standard Error

```json
{
  "timestamp": "2026-06-10T11:00:00+05:30",
  "status": 400,
  "error": "Bad Request",
  "message": "employeeId: must not be blank",
  "path": "/api/auth/signup"
}
```
