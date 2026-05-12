# ExploreWithMe — Event Discovery Platform

A RESTful backend for an event discovery and social planning platform. Users can publish events, find interesting activities, join them as participants, and leave comments. Built as a two-service microservice system with a dedicated statistics service.

## Features

**Public API** — available without registration
- Browse and search events with filtering by category, date, and location
- Sort events by date or view count (fetched from stats service)
- View event compilations curated by admins
- All public endpoint hits are tracked by the stats service

**Private API** — for registered users
- Create, edit and manage own events
- Submit participation requests for events
- Approve or reject incoming participation requests
- Leave comments on events

**Admin API** — for platform administrators
- Manage categories — create, update, delete
- Moderate events — publish or reject user-submitted events
- Manage event compilations on the main page
- Manage users — add, view, delete

## Architecture

ExploreWithMe consists of two independent services:

**Main Service** (`ewm-main-service`) — core platform logic
- Three-tier REST API: public / private / admin
- Event lifecycle management: PENDING → PUBLISHED / CANCELLED
- PostgreSQL as primary database
- H2 in-memory database for tests
- Spring Data JPA + Hibernate for data access
- Spring Actuator for health monitoring
- Integrates with stats service via stats-client

**Stats Service** (`ewm-stats-service`) — analytics microservice
- Records endpoint hit events from main service
- Provides view statistics over date ranges and URIs
- Three internal modules: `stats-server`, `stats-client`, `stats-dto`
- `stats-client` is a reusable HTTP client library consumed by main service
- Independent PostgreSQL instance and Dockerfile

**Request flow:**

`[Client]` → `[Main Service]` ↔ `[Stats Service]`

`[Main PostgreSQL]` — — — — `[Stats PostgreSQL]`

## Tech Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Data JPA + Hibernate** — ORM-based data access
- **PostgreSQL** — primary database (both services)
- **H2** — in-memory database for tests
- **Spring Validation** — declarative request validation
- **Spring Actuator** — health and metrics endpoints
- **Lombok** — boilerplate reduction
- **Docker & Docker Compose** — fully containerized multi-service environment
- **Maven** — multi-module build
- **Checkstyle** — enforced code style
- **JUnit 5 + Mockito** — unit, mock and integration tests
- **Postman** — API testing via collection

## Getting Started

### Requirements
- Docker and Docker Compose installed

### Run locally

```bash
git clone https://github.com/alonazrnko/java-explore-with-me.git
cd java-explore-with-me
docker compose up --build
```

Main service API: `http://localhost:8080`  
Stats service API: `http://localhost:9090`

### Stop the application

```bash
docker compose down
```

### Run tests

```bash
mvn test
```

## API Overview

**Events**

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/events` | Public | Search and filter events |
| GET | `/events/{id}` | Public | Get event details |
| POST | `/users/{userId}/events` | Private | Create an event |
| PATCH | `/users/{userId}/events/{eventId}` | Private | Edit own event |
| GET | `/admin/events` | Admin | Get all events with filters |
| PATCH | `/admin/events/{eventId}` | Admin | Publish or reject event |

**Participation**

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/users/{userId}/requests` | Private | Request to join an event |
| PATCH | `/users/{userId}/events/{eventId}/requests` | Private | Approve or reject requests |

**Comments**

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/users/{userId}/comments/{eventId}` | Private | Add a comment |
| PATCH | `/admin/comments/{commentId}` | Admin | Moderate a comment |

**Categories & Compilations**

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/categories` | Public | Get all categories |
| GET | `/compilations` | Public | Get event compilations |
| POST | `/admin/categories` | Admin | Create a category |
| POST | `/admin/compilations` | Admin | Create a compilation |

## Testing

**Main service tests:**
- Controller tests — `AdminEventControllerTest`, `PrivateEventControllerTest`, `PublicEventControllerTest` and others
- Service tests — unit tests with Mockito for all service implementations
- Repository tests — `EventRepositoryTest`, `RequestRepositoryTest`
- Mapper tests — `EventMapperTest`, `UserMapperTest`, `CategoryMapperTest` and others
- Integration tests — full application context with H2

**Stats service tests:**
- `StatsControllerTest`, `StatsServiceImplTest`, `StatsRepositoryTest`
- Full application context test via `StatsServerAppTest`

**Postman collections** — `ewm-main-service-spec.json` and `ewm-stats-service.json` for manual API testing

## Key Design Decisions

- **Two-service architecture** — stats service is fully independent with its own database, allowing it to scale and deploy separately from the main service
- **stats-client as a shared library** — packaged as a Maven module and imported as a dependency, keeping HTTP communication logic reusable and decoupled
- **Three-tier API** — public / private / admin separation mirrors real-world access control patterns without a gateway in this implementation
- **Event lifecycle** — PENDING → PUBLISHED / CANCELLED state machine enforced at service layer, preventing invalid state transitions
- **H2 for tests** — integration tests run without external dependencies, making CI fast and reliable
- **Comments as additional feature** — designed and implemented independently, integrated into both public browsing and admin moderation flows
