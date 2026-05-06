# ✈️ AirNexus Backend

A cloud-native, microservices-based backend for the AirNexus airline management and booking platform. Built with Spring Boot and Spring Cloud, it exposes a unified REST API through an API Gateway and uses Eureka for service discovery, RabbitMQ for asynchronous event-driven messaging, Redis for caching, MySQL for persistence, and Razorpay for payment processing.

---

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Microservices](#microservices)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Service Port Reference](#service-port-reference)
- [Environment Variables](#environment-variables)
- [API Gateway & Routing](#api-gateway--routing)
- [Authentication & Security](#authentication--security)
- [Messaging (RabbitMQ)](#messaging-rabbitmq)
- [Caching (Redis)](#caching-redis)
- [Payment Integration (Razorpay)](#payment-integration-razorpay)
- [Notification Channels](#notification-channels)
- [Data Models](#data-models)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Testing](#testing)

---

## 🏗 Architecture Overview

```
                             ┌─────────────────┐
                             │  React Frontend  │
                             │  (port 3000)     │
                             └────────┬────────┘
                                      │ HTTP
                             ┌────────▼────────┐
                             │   API Gateway   │  ← JWT filter, CORS, routing
                             │   (port 8080)   │
                             └────────┬────────┘
                                      │ lb:// (Eureka)
         ┌──────────────────┬─────────┴──────┬───────────────────┐
         │                  │                │                   │
┌────────▼────┐  ┌──────────▼──┐  ┌─────────▼─┐  ┌─────────────▼──┐
│Auth Service │  │Flight Service│  │  Booking  │  │  Passenger Svc │
│  (8081)     │  │   (8082)     │  │  (8084)   │  │    (8085)      │
└─────────────┘  └──────┬──────┘  └─────┬─────┘  └────────────────┘
                         │  ▲            │  ▲
                         │  │ cache      │  │ cache
                         ▼  │            ▼  │
                   ┌─────────────┐  ┌──────────────────────┐
                   │   Redis     │  │      RabbitMQ        │
                   │  (6379)     │  │  airnexus.exchange   │
                   └─────────────┘  └──────────┬───────────┘
                         ▲                     │ notification.queue
                         │ cache               ▼
┌─────────────┐  ┌───────┴──────┐  ┌──────────────────────┐
│Seat Service │  │Payment Service│  │  Notification Svc    │
│  (8083)     │  │   (8086)     │  │     (8087)           │
└─────────────┘  └──────────────┘  └──────────────────────┘
                       │  publishes                (Email + SMS)
                       └──────────► RabbitMQ (see above)

┌─────────────┐  ┌──────────────────────────────────────────────┐
│Airline Svc  │  │              Eureka Server (8761)            │
│  (8088)     │  │          Service Registry & Discovery         │
└─────────────┘  └──────────────────────────────────────────────┘
```

**Infrastructure at a glance:**
- **Eureka (8761)** — all services register on startup; the gateway uses `lb://` URIs for client-side load balancing
- **RabbitMQ (5672)** — `booking-service` and `payment-service` publish `NotificationEvent` messages to the `airnexus.exchange` topic exchange; `notification-service` consumes from `notification.queue`
- **Redis (6379)** — `flight-service` and `booking-service` use Redis as a cache layer (`spring.cache.type=redis`) to reduce repeated database hits; `seat-service` uses JPA optimistic locking (`@Version`) instead of Redis to prevent double-booking races

---

## 🧩 Microservices

| Service | Port | Database | Description |
|---|---|---|---|
| **eureka-server** | 8761 | — | Service registry and discovery |
| **api-gateway** | 8080 | — | Unified entry point; JWT auth filter, CORS, routing |
| **auth-service** | 8081 | `airnexus_auth` | User registration, login, JWT issuance, Google OAuth2 |
| **flight-service** | 8082 | `airnexus_flight` | Flight CRUD, search, status management |
| **seat-service** | 8083 | `airnexus_seat` | Seat map, hold/release/confirm lifecycle, optimistic locking |
| **booking-service** | 8084 | `airnexus_booking` | Booking creation, PNR generation, fare calculation |
| **passenger-service** | 8085 | `airnexus_passenger` | Passenger records, seat assignment, check-in |
| **payment-service** | 8086 | `airnexus_payment` | Razorpay order creation, payment verification, refunds |
| **notification-service** | 8087 | `airnexus_notification` | Email (Gmail SMTP) + SMS (Twilio), RabbitMQ consumer |
| **airline-service** | 8088 | `airnexus_airline` | Airline and airport management |

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | RabbitMQ (via Spring AMQP) |
| Caching | Redis (Spring Data Redis) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Authentication | JWT (JJWT), Spring Security, Google OAuth2 |
| Payments | Razorpay Java SDK |
| Email | JavaMail (Gmail SMTP) |
| SMS | Twilio |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build Tool | Maven |
| Utilities | Lombok |

---

## ✅ Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8
- Redis (default port 6379)
- RabbitMQ (default port 5672)
- A Razorpay test account
- A Google Cloud project with OAuth2 credentials
- A Gmail account with an App Password
- A Twilio account (for SMS)

---

## 🚀 Getting Started

### 1. Set up MySQL databases

Create a separate database for each service:

```sql
CREATE DATABASE airnexus_auth;
CREATE DATABASE airnexus_flight;
CREATE DATABASE airnexus_seat;
CREATE DATABASE airnexus_booking;
CREATE DATABASE airnexus_passenger;
CREATE DATABASE airnexus_payment;
CREATE DATABASE airnexus_notification;
CREATE DATABASE airnexus_airline;
```

### 2. Configure environment

Each service uses `application-dev.properties`. Copy and update the credentials for your environment (see [Environment Variables](#environment-variables) below). The `auth-service` also reads from a `.env` file for secrets.

### 3. Start infrastructure

Ensure MySQL, Redis, and RabbitMQ are running locally before starting any service.

### 4. Start services in order

Services must be started in this sequence so that Eureka is available before the others attempt to register:

```bash
# 1. Service Registry
cd eureka-server && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 2. All other services (each in a separate terminal)
cd api-gateway        && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd auth-service       && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd airline-service    && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd flight-service     && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd seat-service       && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd booking-service    && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd passenger-service  && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd payment-service    && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd notification-service && ./mvnw spring-boot:run
```

> The `notification-service` uses `application.properties` directly (no profile needed).

Once all services are running, the frontend at `http://localhost:3000` will talk to `http://localhost:8080` (the API Gateway), which routes to the appropriate microservice.

---

## 🔌 Service Port Reference

| Service | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Swagger UI (aggregated) | http://localhost:8080/swagger-ui.html |
| Auth Service | http://localhost:8081 |
| Flight Service | http://localhost:8082 |
| Seat Service | http://localhost:8083 |
| Booking Service | http://localhost:8084 |
| Passenger Service | http://localhost:8085 |
| Payment Service | http://localhost:8086 |
| Notification Service | http://localhost:8087 |
| Airline Service | http://localhost:8088 |

---

## 🔐 Environment Variables

### auth-service `.env`

```env
CLIENT_ID=<google-oauth2-client-id>
CLIENT_SECRET=<google-oauth2-client-secret>
JWT_SECRET=<hex-encoded-secret-min-32-chars>
```

### Shared across services (`application-dev.properties`)

Each service's dev properties file contains the following. Update with your own credentials:

```properties
# MySQL
spring.datasource.username=root
spring.datasource.password=<your-mysql-password>

# JWT (must be identical across api-gateway and auth-service)
jwt.secret=<hex-encoded-secret>
jwt.expiration=86400000   # 1 day in ms
```

### notification-service (`application.properties`)

```properties
# Gmail SMTP
spring.mail.username=<your-gmail>
spring.mail.password=<gmail-app-password>

# Twilio
twilio.account.sid=<twilio-sid>
twilio.auth.token=<twilio-auth-token>
twilio.phone.number=<twilio-phone>
```

### payment-service (`application.properties`)

```properties
razorpay.key.id=<razorpay-key-id>
razorpay.key.secret=<razorpay-key-secret>
```

> **Security Note:** Never commit real credentials to version control. Use environment variables or a secrets manager in production.

---

## 🌐 API Gateway & Routing

The API Gateway (`port 8080`) is the sole public entry point. It handles:

- **JWT authentication** via `JwtAuthenticationFilter` — validates the Bearer token on all routes except `/auth-service/api/auth/**`
- **CORS** — allows requests from `http://localhost:3000` and `http://localhost:8080`
- **Load-balanced routing** — forwards to microservices by Eureka service name using the `lb://` scheme
- **Prefix stripping** — the service prefix is stripped before forwarding (e.g. `/flight-service/api/flights` → `/api/flights`)

Route map:

| Gateway Prefix | Downstream Service |
|---|---|
| `/auth-service/**` | AUTH-SERVICE |
| `/airline-service/**` | AIRLINE-SERVICE |
| `/flight-service/**` | FLIGHT-SERVICE |
| `/seat-service/**` | SEAT-SERVICE |
| `/booking-service/**` | BOOKING-SERVICE |
| `/passenger-service/**` | PASSENGER-SERVICE |
| `/payment-service/**` | PAYMENT-SERVICE |
| `/notification-service/**` | NOTIFICATION-SERVICE |

---

## 🔑 Authentication & Security

Authentication is managed entirely by the `auth-service` and enforced at the gateway level.

**Registration & Login flow:**
1. Client posts credentials to `/api/auth/register` or `/api/auth/login`
2. `auth-service` validates, hashes the password (BCrypt), and issues a signed JWT
3. The JWT contains `userId`, `email`, and `role`, with a 24-hour expiry
4. All subsequent requests include the JWT as `Authorization: Bearer <token>`
5. The API Gateway's `JwtAuthenticationFilter` validates the signature using the shared secret and injects `X-User-Id` into the forwarded request

**Google OAuth2:**
- Client sends the Google `idToken` to `/api/auth/google/login`
- `OAuth2Service` verifies the token with Google's API and provisions or retrieves the user account

**User roles:**

| Role | Description |
|---|---|
| `GUEST` | Unauthenticated — can search flights |
| `PASSENGER` | Default registered user |
| `AIRLINE_STAFF` | Can manage airlines and airports; linked to a specific airline via `airlineId` |
| `ADMIN` | Full access |

**Auth providers:** `LOCAL` (email/password) and `GOOGLE`

---

## 📨 Messaging (RabbitMQ)

Event-driven notifications are decoupled from the core booking and payment flows via RabbitMQ.

**Configuration:**

| Parameter | Value |
|---|---|
| Exchange | `airnexus.exchange` (topic exchange) |
| Queue | `notification.queue` |
| Routing key | `notification.#` |

**Publishers:**
- `booking-service` — publishes `NotificationEvent` on booking creation and cancellation
- `payment-service` — publishes payment confirmation and refund events

**Consumer:**
- `notification-service` — `NotificationConsumer` listens on `notification.queue`, deserializes the `NotificationEvent`, and dispatches email and SMS notifications accordingly

All messages are serialized as JSON using `Jackson2JsonMessageConverter`.

---

## ⚡ Caching (Redis)

Redis is used in `flight-service` and `booking-service` to cache frequently read data and reduce database load.

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
```

The `seat-service` also uses **optimistic locking** (JPA `@Version`) on the `Seat` entity to prevent double-booking race conditions without Redis.

---

## 💳 Payment Integration (Razorpay)

The `payment-service` integrates with Razorpay (test mode by default).

**Flow:**
1. Client calls `POST /api/payments/initiate` with `bookingId` and `amount`
2. Service creates a Razorpay order and returns `razorpayOrderId` and `key_id` to the client
3. Client completes payment on the Razorpay checkout widget
4. Client calls `POST /api/payments/verify` with the Razorpay `paymentId`, `orderId`, and `signature`
5. Service verifies the HMAC signature, marks the payment `PAID`, and triggers booking confirmation

**Refunds:**
- `POST /api/payments/{id}/refund` with an `amount` query param initiates a partial or full refund via the Razorpay API

**Payment modes supported:** `CARD`, `UPI`, `NET_BANKING`, `WALLET`

**Currency:** INR (default)

---

## 🔔 Notification Channels

The `notification-service` dispatches two types of notifications:

### Email (Gmail SMTP)
Configured via `spring.mail.*` properties. Uses JavaMail with STARTTLS on port 587.

### SMS (Twilio)
Configured via `twilio.*` properties. Sends SMS using the Twilio REST API.

### Scheduled Reminders
A `CheckInReminderScheduler` runs periodically to send check-in reminder notifications to passengers with upcoming flights.

### Notification types stored in DB

Each notification is persisted in `airnexus_notification` with fields: `userId`, `title`, `message`, `type`, `isRead`, `createdAt`.

---

## 🗃 Data Models

### User (auth-service)
`userId`, `fullName`, `email`, `passwordHash`, `phone`, `role` (GUEST/PASSENGER/AIRLINE_STAFF/ADMIN), `provider` (LOCAL/GOOGLE), `isActive`, `passportNumber`, `nationality`, `airlineId`, `createdAt`

### Flight (flight-service)
`flightId` (UUID), `flightNumber`, `airlineId`, `originAirportCode`, `destinationAirportCode`, `departureTime`, `arrivalTime`, `durationMinutes`, `status` (ON_TIME/DELAYED/CANCELLED/DEPARTED/ARRIVED), `aircraftType`, `totalSeats`, `availableSeats`, `basePrice`

### Seat (seat-service)
`seatId` (UUID), `flightId`, `seatNumber`, `seatClass` (ECONOMY/BUSINESS/FIRST), `seat_row`, `seat_column`, `isWindow`, `isAisle`, `hasExtraLegroom`, `status` (AVAILABLE/HELD/CONFIRMED/BLOCKED), `priceMultiplier`, `holdTime`, `heldByUserId`, `version` (optimistic lock)

### Booking (booking-service)
`bookingId` (UUID), `userId`, `flightId`, `pnrCode` (6-char unique), `tripType` (ONE_WAY/ROUND_TRIP), `status` (PENDING/CONFIRMED/CANCELLED/COMPLETED/NO_SHOW), `totalFare`, `baseFare`, `taxes`, `ancillaryCharges`, `mealPreference`, `luggageKg`, `contactEmail`, `contactPhone`, `numberOfPassengers`, `paymentId`, `bookedAt`

### Payment (payment-service)
`paymentId` (UUID), `bookingId`, `userId`, `amount`, `currency` (INR), `status` (PENDING/PAID/FAILED/REFUNDED/PARTIALLY_REFUNDED), `paymentMode` (CARD/UPI/NET_BANKING/WALLET), `transactionId`, `razorpayOrderId`, `gatewayResponse`, `paidAt`, `refundedAt`, `refundAmount`, `createdAt`

---

## 📖 API Documentation (Swagger)

All eight services expose their OpenAPI specs through the aggregated Swagger UI served by the API Gateway:

**➡ http://localhost:8080/swagger-ui.html**

Use the dropdown in the top-right corner to switch between service specs (Auth, Airline, Flight, Seat, Booking, Passenger, Payment, Notification).

Individual service docs are also available directly (e.g. `http://localhost:8082/swagger-ui.html` for flight-service).

---

## 🧪 Testing

Tests are located in `src/test/` within each service module.

```bash
# Run tests for a specific service
cd airline-service
./mvnw test
```

The `airline-service` includes a comprehensive `AirlineServiceImplTest` covering the service layer with mocked repositories. Other services include Spring Boot context load tests.

---

## 📄 License

This project is private. All rights reserved.
