# Controller Implementation Guide

This guide provides examples on how to implement controllers using the provided base classes, `BaseController` and `ResilienceBaseController`.

## 1. Using `BaseController`

`BaseController` is the standard choice for creating REST controllers. It provides helper methods to build consistent JSON responses.

### Key Features
- **`createResponse(T data, HttpStatus status)`**: Creates a simple `ResponseEntity` with the provided data and HTTP status.
- **`createResponse(T data, HttpStatus status, String code, String message)`**: Creates a structured `ResponseEntity<ResponseData<T>>` which includes status, code, message, and the data payload. This is the recommended method for standardized API responses.

### Example Implementation

Here is an example of a simple controller for managing users.

```java
package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.controller.BaseController;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<User>> getUserById(@PathVariable String id) {
        User user = userService.findUserById(id);
        
        // Use the helper to create a standardized success response
        return createResponse(
            user,
            HttpStatus.OK,
            "00", // Custom success code
            "User retrieved successfully."
        );
    }

    @PostMapping
    public ResponseEntity<ResponseData<User>> createUser(@RequestBody User newUser) {
        User createdUser = userService.createUser(newUser);

        // Use the helper to create a standardized "Created" response
        return createResponse(
            createdUser,
            HttpStatus.CREATED,
            "00",
            "User created successfully."
        );
    }
}
```

## 2. Using `ResilienceBaseController`

`ResilienceBaseController` extends `BaseController` and adds capabilities from the **Resilience4j** library, such as Rate Limiter and Circuit Breaker patterns, directly into the response creation flow.

This is useful for protecting your endpoints from being overwhelmed or for handling failures gracefully.

### Prerequisites

To use `ResilienceBaseController`, you must provide the following beans in your Spring application context:
- `ResilienceService`
- `RateLimiterRegistry`
- `CircuitBreakerRegistry`

These are typically configured in a central configuration class.

### Key Features
- **`createResponseWithRateLimiter(...)`**: Wraps the response creation in a Rate Limiter. If the request rate is exceeded, it automatically returns an `HTTP 429 Too Many Requests` response.
- **`createResponseWithCircuitBreaker(...)`**: Wraps the response creation in a Circuit Breaker. If the downstream service is failing, the circuit will "open" and subsequent calls will immediately fail without calling the service, returning an `HTTP 500 Internal Server Error`.

### Example Implementation

This example shows a controller that protects its endpoints using both Rate Limiter and Circuit Breaker patterns.

```java
package com.example.controller;

import com.example.model.Product;
import com.example.service.ProductService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.controller.ResilienceBaseController;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

import java.time.Duration;

@RestController
@RequestMapping("/api/products")
public class ProductController extends ResilienceBaseController {

    private final ProductService productService;

    public ProductController(ResilienceService resilienceService, RateLimiterRegistry rateLimiterRegistry, CircuitBreakerRegistry circuitBreakerRegistry, ProductService productService) {
        super(resilienceService, rateLimiterRegistry, circuitBreakerRegistry);
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<Product>> getProductById(@PathVariable String id) {
        // Configure a Rate Limiter: 10 requests per minute for this specific endpoint.
        RateLimiterConfig rlConfig = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build();

        // The business logic is wrapped inside a lambda.
        Product product = productService.findProductById(id);

        // The response is created with the rate limiter.
        return createResponseWithRateLimiter(
            product,
            "00",
            "Product retrieved.",
            rlConfig,
            "getProductById" // A unique key for this rate limiter
        );
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<ResponseData<String>> purchaseProduct(@PathVariable String id) {
        // Configure a Circuit Breaker for a potentially unstable downstream service.
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Open circuit if 50% of requests fail
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
                .build();

        // Business logic that calls an external payment service.
        String confirmation = productService.purchase(id);

        // The response is created with the circuit breaker.
        return createResponseWithCircuitBreaker(
            confirmation,
            "00",
            "Purchase successful.",
            cbConfig,
            "purchaseService" // A unique name for this circuit breaker
        );
    }
}
```
