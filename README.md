# URL Shortener

A production-oriented URL shortening REST API built with Spring Boot.

The application generates short URLs for long URLs, redirects users to the original URLs, tracks access counts using Redis, persists data in MySQL, and periodically synchronizes Redis data back to MySQL.

## Features

- Create short URLs from long URLs
- Redirect short URLs to their original URLs
- SHA-256 based duplicate URL detection
- Configurable URL expiration
- Automatic cleanup of expired URLs
- Redis caching for fast URL lookups
- Redis-based access counting
- Periodic synchronization of access counts to MySQL
- Redis fallback to MySQL when Redis is unavailable
- IP-based rate limiting
- Request validation
- Collision-safe short-code generation
- Global exception handling
- Database schema versioning with Flyway
- REST API documentation with Swagger/OpenAPI
- Application health monitoring with Spring Boot Actuator
- Dockerized application
- Docker Compose deployment
- Nginx reverse proxy
- HTTPS/TLS support
- HTTP to HTTPS redirection
- Non-root application container
- Production resource and log limits

## Architecture

## Architecture

The application follows a layered architecture with MySQL as the persistent data store and Redis as the caching and high-speed access-counting layer.

### High-Level Architecture

```text
                    Client
                      │
                      ▼
               ┌─────────────┐
               │    Nginx    │
               │ Reverse     │
               │ Proxy + TLS │
               └──────┬──────┘
                      │
                      ▼
              ┌───────────────┐
              │  Spring Boot  │
              │      API      │
              └───────┬───────┘
                      │
          ┌───────────┴───────────┐
          │                       │
          ▼                       ▼
   ┌──────────────┐       ┌──────────────┐
   │    MySQL     │       │    Redis     │
   │              │       │              │
   │ Persistent   │       │ URL Cache    │
   │ Data Store   │       │ Access Count │
   └──────────────┘       └──────────────┘
```

### Creating the Short URL

Client
  │
  │ POST /api/url-shortener
  ▼
Nginx
  │
  ▼
Spring Boot
  │
  ├── Validate request
  │
  ├── Apply rate limit
  │
  ├── Generate SHA-256 hash
  │
  ├── Check MySQL for an existing active URL
  │
  ├── Generate unique short code
  │
  ├── Save URL to MySQL
  │
  └── Cache URL in Redis
  │
  ▼
201 Created

### Redirecting a Short URL

Client
  │
  │ GET /nifty/{shortCode}
  ▼
Nginx
  │
  ▼
Spring Boot
  │
  ├── Check Redis
  │      │
  │      ├── Cache hit ──► Return original URL
  │      │
  │      └── Cache miss
  │              │
  │              ▼
  │           Check MySQL
  │
  ├── Increment access count in Redis
  │
  └── Return 302 redirect

## Tech Stack

### Backend

- **Java 21**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Spring Data Redis**
- **Bean Validation**
- **Spring Boot Actuator**
- **SpringDoc OpenAPI / Swagger**

### Databases & Storage

- **MySQL 8.4** — persistent URL data
- **Redis** — caching, access counters, and rate limiting
- **Flyway** — database schema migrations

### Infrastructure

- **Docker**
- **Docker Compose**
- **Nginx** — reverse proxy and TLS termination
- **HTTPS/TLS** — secure client communication

### Testing & Development

- **JUnit**
- **Mockito**
- **MockMvc**
- **Postman**
- **Swagger UI**

### Build Tool

- **Maven**

## Project Structure

```text
url-shortener/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migrations/
│   │       └── application.properties
│   │
│   └── test/
│       ├── java/
│       │   └── ...
│       └── resources/
│           └── application-test.properties
│
├── nginx/
│   ├── nginx.conf
│   └── certs/
│
├── .dockerignore
├── Dockerfile
├── compose.yml
├── compose.prod.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md

## API Documentation

The API is available through the HTTPS reverse proxy.

Base URL:

```text
https://localhost

Swagger Documentation
https://localhost/swagger-ui/index.html

```

## Error Handling

The application uses a global exception handler to return consistent JSON error responses.

### Error Response Format

```json
{
  "message": "Url not found."
}
```

## Rate Limiting Flow
Request
   │
   ▼
Get client IP
   │
   ▼
Increment Redis counter
   │
   ├── Count ≤ 5 ──► Allow request
   │
   └── Count > 5 ──► 429 Too Many Requests
