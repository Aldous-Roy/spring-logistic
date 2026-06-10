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
