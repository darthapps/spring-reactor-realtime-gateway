# Real-Time Payment Gateway

This project implements a high-performance, reactive payment gateway API designed to handle instant transfer requests. It orchestrates validation across multiple internal services (Risk, Ledger) and executes transactions on a core banking system, ensuring low latency and high resilience.

## Architecture Overview

The service exposes a RESTful API built with **Spring WebFlux** to handle high concurrency. It employs a non-blocking, event-driven architecture to perform parallel validations before committing transactions.

### Key Features
*   **Reactive Stack:** Built on Project Reactor for non-blocking I/O.
*   **Parallel Processing:** Executes Risk and Ledger checks concurrently to minimize response time.
*   **Resilience:** Implements timeouts and fallback strategies for flaky external dependencies (e.g., Risk Service).
*   **Graceful Error Handling:** Maps domain-specific errors (Insufficient Funds, High Risk) to appropriate HTTP status codes.

## API Specification

### Create Payment
Initiates a new payment transfer.

**Endpoint:** `POST /api/v1/payments`

**Request Body:**
```json
{
  "userId": "user_123",
  "amount": 500.00,
  "targetAccount": "acc_999"
}
```

**Response Codes:**
*   `200 OK`: Payment accepted and processed.
*   `403 Forbidden`: Transaction rejected due to high risk.
*   `422 Unprocessable Entity`: Insufficient funds.
*   `404 Not Found`: User does not exist.

## Logic Flow

1.  **User Validation:** Verifies the existence of the user.
2.  **Parallel Validation:**
    *   **Risk Assessment:** Queries the `RiskService`. If the service is slow (>500ms) or fails, it defaults to a high-risk safety fallback.
    *   **Funds Check:** Queries the `LedgerService` to ensure sufficient balance.
3.  **Transaction Execution:** Upon successful validation, the transaction is persisted to the core banking system.

## Technology Stack

*   **Java 21**
*   **Spring Boot 3.x** (WebFlux)
*   **MongoDB** (Reactive Driver)
*   **Docker**
*   **Maven**

## Getting Started

For detailed setup, running instructions, and testing commands, please refer to [INSTRUCTIONS.md](INSTRUCTIONS.md).