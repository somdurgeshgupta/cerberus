# Cerberus

A Spring Boot REST API application for managing **Products** and **Orders**, backed by PostgreSQL and secured with HTTP Basic authentication.

## Tech Stack

- **Java**: 25
- **Framework**: Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation, Spring Boot Actuator)
- **Database**: PostgreSQL
- **Build Tool**: Maven (with included Maven Wrapper `mvnw` / `mvnw.cmd`)

## Project Structure

```text
src/main/java/com/example/ingenieur/cerberus/
├── CerberusApplication.java
├── config/
│   └── SecurityConfig.java              # HTTP Basic auth & CSRF configuration for /api/**
├── Controller/
│   ├── ProductController.java           # /api/products endpoints
│   └── OrderController.java             # /api/orders endpoints
├── Service/
│   └── OrderService.java                # Order placement, lookup, total calculation & deletion
├── Repository/
│   ├── ProductRepository.java           # JPA repository for Product
│   └── OrderRepository.java             # JPA repository for Order
├── Entity/
│   ├── Product.java                     # Product entity
│   ├── Order.java                       # Order entity (table: orders)
│   └── OrderStatus.java                 # OrderStatus enum (PLACED, CANCELLED)
├── dto/
│   ├── PlaceOrderRequest.java           # Request payload with validation
│   └── OrderResponse.java               # Order response payload
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java      # Consistent 400 and 404 JSON error responses
```

## Accessing from GitHub

Clone the repository and navigate into the project directory:

```bash
git clone https://github.com/somdurgeshgupta/cerberus.git
cd cerberus
```

## Prerequisites

1. **Java Development Kit (JDK) 25** installed and available on `PATH` (`java -version`).
2. **PostgreSQL** running locally on port `5432` (or update the connection settings below).
3. *(Optional)* **Git** and an API client such as `curl` or Postman. No global Maven installation is required because the project includes the Maven Wrapper.

## Database & Application Configuration

Default configuration is located in `src/main/resources/application.properties`:

| Property | Default Value | Description |
| --- | --- | --- |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/cerberus_local_db` | PostgreSQL JDBC URL |
| `spring.datasource.username` | `postgres` | PostgreSQL username |
| `spring.datasource.password` | `1234` | PostgreSQL password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creates/updates `product` and `orders` tables |
| `spring.security.user.name` | `apiuser` | HTTP Basic auth username |
| `spring.security.user.password` | `dev-password-123` | HTTP Basic auth password |

### Create the Local Database

Before running the application for the first time, create the database in PostgreSQL:

```sql
CREATE DATABASE cerberus_local_db;
```

You can override any default configuration using Spring Boot environment variables (for example `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_SECURITY_USER_NAME`, `SPRING_SECURITY_USER_PASSWORD`) or by editing `src/main/resources/application.properties`.

## Running the Application

Start the application on `http://localhost:8080`:

- **Windows (PowerShell / CMD)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

To build an executable JAR and run it directly:

```bash
# Build (macOS/Linux; use .\mvnw.cmd on Windows)
./mvnw clean package

# Run the packaged JAR
java -jar target/cerberus-0.0.1-SNAPSHOT.jar
```

## Running Tests

Run the full test suite (requires PostgreSQL running for `CerberusApplicationTests`):

- **Windows**:
  ```powershell
  .\mvnw.cmd test
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw test
  ```

To run only the unit and MockMvc controller tests without requiring a running PostgreSQL instance:

```bash
./mvnw test -Dtest=ProductControllerTests,OrderControllerTests,OrderServiceTests
```

## Authentication

All endpoints require **HTTP Basic Authentication**. CSRF protection is disabled for `/api/**` routes.

- **Username**: `apiuser`
- **Password**: `dev-password-123`

## API Reference

Base URL: `http://localhost:8080`

### Products (`/api/products`)

| Method | Endpoint | Description | Request Body |
| --- | --- | --- | --- |
| `POST` | `/api/products` | Create a product | `{"name", "description", "price"}` |
| `GET` | `/api/products` | List all products | — |
| `GET` | `/api/products/{id}` | Get product by ID | — |
| `PUT` | `/api/products/{id}` | Update a product | `{"name", "description", "price"}` |
| `DELETE` | `/api/products/{id}` | Delete a product | — |

### Orders (`/api/orders`)

| Method | Endpoint | Description | Request Body |
| --- | --- | --- | --- |
| `POST` | `/api/orders` | Place an order (`201 Created`) | `{"productId", "quantity"}` (`quantity >= 1`) |
| `GET` | `/api/orders` | List all orders | — |
| `GET` | `/api/orders/{id}` | Get order by ID | — |
| `DELETE` | `/api/orders/{id}` | Delete an order | — |

### Quick API Examples (`curl`)

1. **Create a product**:
   ```bash
   curl -u apiuser:dev-password-123 \
     -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Keyboard","description":"Mechanical keyboard","price":79.99}'
   ```

2. **Place an order**:
   ```bash
   curl -u apiuser:dev-password-123 \
     -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"productId":1,"quantity":2}'
   ```

   Example response (`201 Created`):
   ```json
   {
     "id": 1,
     "productId": 1,
     "productName": "Keyboard",
     "unitPrice": 79.99,
     "quantity": 2,
     "totalPrice": 159.98,
     "status": "PLACED",
     "createdAt": "2026-11-11T22:00:00.000000"
   }
   ```

3. **List all orders**:
   ```bash
   curl -u apiuser:dev-password-123 http://localhost:8080/api/orders
   ```

4. **Delete an order**:
   ```bash
   curl -u apiuser:dev-password-123 \
     -X DELETE http://localhost:8080/api/orders/1
   ```

   Example response (`200 OK`):
   ```json
   {
     "message": "Order deleted successfully",
     "id": 1
   }
   ```

