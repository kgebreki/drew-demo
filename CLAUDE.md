# Multi-Agent Order System

## Project Overview

A barebones microservices order system built with plain Java (no frameworks). Three services communicate over HTTP to demonstrate multi-agent development.

## Architecture

```
┌────────────────────┐       ┌────────────────────┐
│  Product Service   │◄──────│   Order Service    │
│   localhost:8081   │  HTTP │   localhost:8082   │
└────────────────────┘       └────────────────────┘
         ▲                            ▲
         │         ┌─────────────────┐│
         └─────────│Integration Tests├┘
                   └─────────────────┘
```

## Shared API Contract

### Product Service (port 8081)

**GET /products**
Returns all products.
```json
[
  { "id": 1, "name": "Laptop", "price": 999.99 },
  { "id": 2, "name": "Mouse", "price": 24.99 },
  { "id": 3, "name": "Keyboard", "price": 74.99 },
  { "id": 4, "name": "Monitor", "price": 349.99 },
  { "id": 5, "name": "Headphones", "price": 149.99 }
]
```

**GET /products/{id}**
Returns a single product or HTTP 404 with:
```json
{ "error": "Product not found" }
```

### Order Service (port 8082)

**POST /orders**
Request body:
```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

Success response (HTTP 201):
```json
{
  "orderId": "ORD-1",
  "items": [
    { "productId": 1, "name": "Laptop", "price": 999.99, "quantity": 2, "subtotal": 1999.98 },
    { "productId": 3, "name": "Keyboard", "price": 74.99, "quantity": 1, "subtotal": 74.99 }
  ],
  "total": 2074.97
}
```

Error — invalid product (HTTP 400):
```json
{ "error": "Product not found: 999" }
```

Error — empty items (HTTP 400):
```json
{ "error": "Order must contain at least one item" }
```

**GET /orders/{orderId}**
Returns the order or HTTP 404 with:
```json
{ "error": "Order not found" }
```

---

## Tech Stack & Constraints

- **Java 17+** — no external frameworks
- **Maven** — multi-module project with a parent POM at the root
- **`com.sun.net.httpserver.HttpServer`** — for HTTP (built into the JDK)
- **JSON handling** — write a minimal hand-rolled JSON utility (no Gson/Jackson). Keep it simple: only handle flat objects and arrays of flat objects.
- **In-memory storage only** — no databases, no files
- Each module must be runnable via `mvn -pl <module> clean compile exec:java`

---

## Java Best Practices

All agents must follow these conventions:

### Code Organization
- One public class per file, filename matches class name
- Use packages: `com.demo.product`, `com.demo.order`, `com.demo.testing`
- Keep classes small and focused — single responsibility

### Naming Conventions
- Classes: `PascalCase` (e.g., `ProductHandler`, `OrderService`)
- Methods and variables: `camelCase` (e.g., `findById`, `totalPrice`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PORT`)
- Packages: all lowercase

### Code Quality
- All fields should be `private`; use getters where needed
- Use `final` for fields that don't change after construction
- Prefer immutability — don't expose mutable internal state
- No raw types — always parameterize generics (e.g., `List<Product>` not `List`)
- Handle exceptions properly — don't swallow exceptions with empty catch blocks
- Close resources properly (use try-with-resources for streams/readers)
- Validate inputs at service boundaries (handlers)

### HTTP Handler Pattern
Each handler should follow this structure:
```java
public class SomeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // 1. Check HTTP method
            // 2. Parse request (path params, body)
            // 3. Call service logic
            // 4. Send JSON response with correct status code
        } catch (Exception e) {
            // Send error JSON response
        }
    }
}
```

### Documentation
- Add a Javadoc comment on every public class explaining its purpose
- Add a brief comment on non-obvious logic
- Don't over-comment obvious code

### Error Handling
- Return proper HTTP status codes (200, 201, 400, 404, 500)
- Always return JSON error bodies — never empty responses or stack traces
- Log errors to `System.err`

---

## Project Structure

```
/
├── CLAUDE.md
├── pom.xml                              (parent POM, defines modules)
│
├── product-service/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/demo/product/
│           ├── ProductServer.java       (main, starts HttpServer)
│           ├── ProductHandler.java      (handles /products and /products/{id})
│           ├── Product.java             (model)
│           ├── ProductRepository.java   (in-memory store)
│           └── JsonUtil.java            (minimal JSON serialization)
│
├── order-service/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/demo/order/
│           ├── OrderServer.java         (main, starts HttpServer)
│           ├── OrderHandler.java        (handles /orders and /orders/{id})
│           ├── Order.java               (model)
│           ├── OrderItem.java           (model)
│           ├── OrderService.java        (business logic, calls product service)
│           ├── ProductClient.java       (HTTP client to product-service)
│           └── JsonUtil.java            (minimal JSON serialization)
│
└── integration-tests/
    ├── pom.xml
    └── src/main/java/
        └── com/demo/testing/
            ├── TestRunner.java          (main, runs all tests)
            ├── ProductServiceTest.java  (tests product endpoints)
            ├── OrderServiceTest.java    (tests order endpoints)
            └── HttpTestClient.java      (simple HTTP helper for tests)
```

---

## Agent Tasks

### Agent 1 — Product Service
**Branch:** `feature/product-service`

Build the product service as specified above. Hardcode the 5 products listed in the contract. Ensure the service starts on port 8081 and responds correctly to both endpoints. The parent `pom.xml` and module `pom.xml` already exist — do NOT modify them. The service should be runnable via `mvn -pl product-service exec:java`.

### Agent 2 — Order Service
**Branch:** `feature/order-service`

Build the order service as specified above. It must call the product service at `http://localhost:8081` to look up product details when creating an order. Generate order IDs sequentially as `ORD-1`, `ORD-2`, etc. Store orders in memory so they can be retrieved by ID. The parent `pom.xml` and module `pom.xml` already exist — do NOT modify them. The service should be runnable via `mvn -pl order-service exec:java`.

### Agent 3 — Integration Tests
**Branch:** `feature/integration-tests`

> **NOTE:** Only start this task after Agent 1 and Agent 2 have merged to main.

Write integration tests that start both services, exercise all endpoints, and verify correct behavior. The parent `pom.xml` and module `pom.xml` already exist — do NOT modify them. Tests should be runnable via `mvn -pl integration-tests exec:java`. Tests should cover:
1. GET /products — returns all 5 products
2. GET /products/{id} — returns correct product
3. GET /products/999 — returns 404
4. POST /orders — valid order with multiple items, verify total calculation
5. POST /orders — order with invalid product ID, verify 400
6. POST /orders — empty items list, verify 400
7. GET /orders/{id} — retrieve a previously created order
8. GET /orders/ORD-999 — returns 404

The test runner should print clear PASS/FAIL output and exit with code 0 on success, 1 on failure. If any test fails, **debug the issue**: read the error, identify whether it's in the product service, order service, or the test itself, and fix it.
