# Service Implementation Guide

This guide provides examples on how to implement services using the provided base builders.

## 1. Using `BaseServiceBuilder` (Stateful Content Object)

This pattern uses a mutable `content` object to pass state between different processing steps. It's straightforward but requires careful management of the execution order.

```java
package com.example.service;

import org.springframework.stereotype.Service;
import reyga.starter.foundation.common.model.dto.content.EmptyContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.service.base.BaseServiceBuilder;

import java.util.Map;
import java.util.UUID;

@Service
public class SampleServiceBuilder extends BaseServiceBuilder<SampleServiceBuilder, BaseRequest, String, EmptyContent> {

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }

    @Override
    protected String orchestrate(BaseRequest req, EmptyContent content) {
        return this.service(req, content)
                .validateRequest()
                    .addProcess(this::process1)
                    .addProcess(this::process2)
                    .addProcess(() -> setProcess(content))
                    .addProcess(() -> getProcess(content))
                    .endProcess(this::responseProcess)
                .build();
    }

    private void process1() {
        if (logger != null) {
            logger.info("Executing Step 1: Validating business rules / Data modification...");
        }
    }

    private boolean process2() {
        if (logger != null) {
            logger.info("Executing Step 2: Saving to database or calling external API...");
        }
        return true;
    }

    private void setProcess(EmptyContent content) {
        content.setNodeId(Map.of("NODE01", UUID.randomUUID()));
    }

    private void getProcess(EmptyContent content) {
        logger.info(String.valueOf(content.getNodeId()));
    }

    private String responseProcess() {
        if (logger != null) {
            logger.info("Executing Final Step: Returning result");
        }
        BaseRequest finalRequest = this.getRequest();
        String requestId = finalRequest.getServletRequest() != null ? finalRequest.getServletRequest().getRequestId() : "N/A";
        return "SUCCESS Processed Request ID: " + requestId;
    }
}
```

## 2. Using `PipelineServiceBuilder` (Functional Chaining)

This pattern provides a type-safe, functional pipeline where the output of one step becomes the input for the next. It's recommended for complex workflows.

### Example 1: Synchronous Transactional Workflow

```java
package com.example.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import reyga.starter.foundation.common.model.dto.content.EmptyContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.service.base.PipelineServiceBuilder;

import java.util.UUID;

@Service
public class ComplexOrderService extends PipelineServiceBuilder<ComplexOrderService, ComplexOrderService.CreateOrderRequest, ComplexOrderService.OrderConfirmation, EmptyContent> {

    @Setter @Getter
    public static class CreateOrderRequest extends BaseRequest {
        private String userId;
        private String productId;
        private int quantity;
    }

    public record OrderConfirmation(String orderId, String userName, String productName, int quantity, String status, String notes) {}
    private record User(String id, String name, String email) {}
    private record Product(String id, String name, double price, int stock) {}
    private record InitialFetchedData(User user, Product product, int quantity) {}
    private record ValidatedOrder(User user, Product product, int quantity, double totalPrice) {}
    private record SavedOrder(String orderId, User user, Product product, int quantity, double totalPrice) {}

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }

    @Override
    protected OrderConfirmation orchestrate(CreateOrderRequest req, EmptyContent content) {
        return this.service(req, content)
                .withTransactional()
                .onTransactionalFallback(this::handleOrderCreationFailure)
                    .startPipe(initialState -> initialState)
                        .doValidationRequest()
                        .andThen(this::fetchUserAndProduct)
                        .andThen(this::checkStockAndCalculatePrice)
                        .andThen(this::saveOrderToDatabase)
                        .andThen(this::sendConfirmationEmail)
                        .andThen(this::createFinalResponse)
                    .build();
    }

    private InitialFetchedData fetchUserAndProduct(InitialState<CreateOrderRequest, EmptyContent> initialState) {
        // ... implementation
    }

    private ValidatedOrder checkStockAndCalculatePrice(InitialFetchedData data) {
        // ... implementation
    }

    private SavedOrder saveOrderToDatabase(ValidatedOrder order) {
        // ... implementation
    }

    private SavedOrder sendConfirmationEmail(SavedOrder savedOrder) {
        // ... implementation
    }

    private OrderConfirmation createFinalResponse(SavedOrder savedOrder) {
        // ... implementation
    }

    private OrderConfirmation handleOrderCreationFailure(Throwable throwable) {
        // ... implementation
    }
}
```

### Example 2: Mixed Synchronous and Asynchronous Workflow

This example shows how to trigger a "fire-and-forget" asynchronous task within the pipeline.

```java
package com.example.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import reyga.starter.foundation.common.model.dto.content.EmptyContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.service.base.PipelineServiceBuilder;

import java.util.UUID;

@Service
public class AsyncComplexOrderService extends PipelineServiceBuilder<AsyncComplexOrderService, AsyncComplexOrderService.CreateOrderRequest, AsyncComplexOrderService.OrderConfirmation, EmptyContent> {

    @Setter @Getter
    public static class CreateOrderRequest extends BaseRequest {
        private String userId;
        private String productId;
        private int quantity;
    }

    public record OrderConfirmation(String orderId, String userName, String productName, int quantity, String status, String notes) {}
    private record User(String id, String name, String email) {}
    private record Product(String id, String name, double price, int stock) {}
    private record InitialFetchedData(User user, Product product, int quantity) {}
    private record ValidatedOrder(User user, Product product, int quantity, double totalPrice) {}
    private record SavedOrder(String orderId, User user, Product product, int quantity, double totalPrice) {}

    @Override
    protected boolean useHttpServletParameter() {
        return false;
    }

    @Override
    protected OrderConfirmation orchestrate(CreateOrderRequest req, EmptyContent content) {
        return this.service(req, content)
                .withTransactional()
                .onTransactionalFallback(this::handleOrderCreationFailure)
                    .startPipe(initialState -> initialState)
                        .doValidationRequest()
                        .andThen(this::fetchUserAndProduct)
                        .andThen(this::checkStockAndCalculatePrice)
                        .andThen(this::saveOrderToDatabase)
                        .andThenAsync(this::sendConfirmationEmail) // Fire-and-forget email sending
                        .andThen(this::createFinalResponse)
                    .build();
    }

    private InitialFetchedData fetchUserAndProduct(InitialState<CreateOrderRequest, EmptyContent> initialState) {
        // ... implementation
    }

    private ValidatedOrder checkStockAndCalculatePrice(InitialFetchedData data) {
        // ... implementation
    }

    private SavedOrder saveOrderToDatabase(ValidatedOrder order) {
        // ... implementation
    }

    private void sendConfirmationEmail(SavedOrder savedOrder) {
        // ... implementation (e.g., Thread.sleep(2000))
    }

    private OrderConfirmation createFinalResponse(SavedOrder savedOrder) {
        // ... implementation
    }

    private OrderConfirmation handleOrderCreationFailure(Throwable throwable) {
        // ... implementation
    }
}
```
