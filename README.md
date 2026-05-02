# Money Manager API

A RESTful backend API for personal finance management — track expenses, incomes, and categories with JWT-secured endpoints and built-in Excel/email reporting.

## Tech Stack

- **Java 17** · Spring Boot 4.0.2
- **Spring Security** with JWT authentication
- **Spring Data JPA** · Hibernate
- **MySQL** (development) · **PostgreSQL** (production via Render.com)
- **Apache POI** — Excel report generation
- **Spring Mail** via Brevo SMTP — email delivery
- **Docker** — multi-stage build

## Features

- User registration with email activation
- JWT authentication (access token: 24h, refresh token: 7d)
- Expense and income tracking with category support
- Dashboard summary metrics
- Export reports to Excel or send via email
- Multi-environment configuration (dev/prod)

## Project Structure

```
src/main/java/com/tiuon/moneymanager/
├── controller/       # REST controllers
├── service/          # Business logic interfaces
├── service/impl/     # Service implementations
├── entity/           # JPA entities
├── repository/       # Spring Data repositories
├── dto/              # Request/response DTOs
├── mapper/           # Entity ↔ DTO mappers
├── security/         # JWT filter & security config
├── config/           # App configuration
├── util/             # JWT utility
└── exception/        # Global exception handling
```

## API Reference

Base path: `/api/v1.0`

### Auth & Profile

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/register` | Register a new user | No |
| GET | `/activate?token={token}` | Activate account via email token | No |
| POST | `/login` | Authenticate and receive JWT tokens | No |
| GET | `/profile` | Get current user's profile | Yes |

### Expenses

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/expenses` | Add a new expense |
| GET | `/expenses` | Get current month's expenses |
| DELETE | `/expenses/{expenseId}` | Delete an expense |
| GET | `/expenses/export/excel` | Download expenses as Excel |
| GET | `/expenses/email` | Email expense report |

### Incomes

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/incomes` | Add a new income |
| GET | `/incomes` | Get current month's incomes |
| DELETE | `/incomes/{incomeId}` | Delete an income |
| GET | `/incomes/export/excel` | Download incomes as Excel |
| GET | `/incomes/email` | Email income report |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/categories` | Create a category |
| GET | `/categories` | Get all categories for current user |
| GET | `/categories/{type}` | Get categories by type (`income`/`expense`) |
| PUT | `/categories/{categoryId}` | Update a category |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Get summary metrics |

All endpoints except auth require a `Bearer <token>` header.

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL 8+ (for local development)

### Run Locally

1. Clone the repository and configure your local database in `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moneymanager
    username: root
    password: root
```

2. Build and run:

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:9090`.

### Run with Docker

```bash
docker build -t moneymanager .
docker run -p 9090:9090 moneymanager
```

### Production

Set the following environment variables for the production profile (`-Dspring.profiles.active=prod`):

| Variable | Description |
|----------|-------------|
| `PROD_DB_HOST` | PostgreSQL host (Render.com) |
| `PROD_DB_USERNAME` | Database username |
| `PROD_DB_PASSWORD` | Database password |

Connect to the production database with [DBeaver](https://dbeaver.io/).

## Database Schema

| Table | Description |
|-------|-------------|
| `tbl_profile` | User accounts with activation state |
| `tbl_categories` | Income/expense categories per user |
| `tbl_expenses` | Expense records linked to categories |
| `tbl_incomes` | Income records linked to categories |

## Running Tests

```bash
./mvnw test
```
