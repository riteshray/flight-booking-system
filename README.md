# Flight Booking System

Spring Boot REST API with JWT authentication, flight search, and booking management. Includes Swagger UI for API testing.

## Tech Stack

- Java 21, Spring Boot 3.2.5, Spring Security, JWT
- H2 Database, Maven, SpringDoc OpenAPI (Swagger)

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+

### Setup & Run
```bash
# Build
mvn clean package

# Run
java -jar target/flight-booking-system-1.0.0.jar
```

## Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# JWT
jwt.secret=256-bit-secret-key
jwt.access.expiration=604800000   # 7 days
jwt.refresh.expiration=2592000000  # 30 days

# Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create
```

**Application URL**: http://localhost:8080

### Access Points
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:testdb`, User: `sa`, Password: empty)

## API Testing with Postman

### Import Collection

1. Open Postman
2. Click **Import** → Select `Flight-Booking-System.postman_collection.json`
3. Collection includes all endpoints with automatic token management

### Quick Start Flow

```bash
# 1. Register or Login (automatically saves tokens)
POST /api/v1/auth/register or /api/v1/auth/login

# 2. Test protected endpoints (token auto-applied)
GET /api/v1/airports
POST /api/v1/flights/search
```

## Testing

```bash
# Run all tests
mvn test

# Skip tests during build
mvn clean package -DskipTests
```

## Authentication

### 1. Register & Get JWT Token
```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com", "name": "John Doe", "password": "password123"}'

# Response includes accessToken and refreshToken
```

### 2. Use Token for Protected Endpoints
```bash
curl -X GET http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Using Swagger UI
1. Open http://localhost:8081/swagger-ui.html
2. Click **"Authorize"** button (lock icon)
3. Enter: `Bearer YOUR_ACCESS_TOKEN`
4. Click **"Authorize"**
5. Test protected endpoints


## API Endpoints

### Authentication (Public)
```bash
# Register
POST /api/v1/auth/register
Body: {"email": "user@example.com", "name": "John Doe", "password": "password123"}

# Login
POST /api/v1/auth/login
Body: {"email": "user@example.com", "password": "password123"}
```

### Airports (Protected - Requires JWT)
```bash
GET /api/v1/airports                    # Get all airports
```

### Flights (Protected - Requires JWT)
```bash
# Search flights
POST /api/v1/flights/search
Body: {"origin": "JFK", "destination": "LAX", "date": "2026-06-15"}

GET /api/v1/flights                    # Get all flights
GET /api/v1/flights/{flightNumber}     # Get flight by flight number
```

### Users (Protected - Requires JWT)
```bash
GET    /api/v1/users                   # Get all users
GET    /api/v1/users/{userId}          # Get user by ID
POST   /api/v1/users                   # Create user
```

### Bookings (Protected - Requires JWT)
```bash
POST   /api/v1/bookings                               # Create booking
GET    /api/v1/bookings/reference/{bookingReference}  # Get booking by reference
GET    /api/v1/bookings/my-bookings                   # Get user's bookings
PUT    /api/v1/bookings/{bookingReference}/cancel     # Cancel booking
```
---
