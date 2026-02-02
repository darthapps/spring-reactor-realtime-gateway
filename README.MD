# The Challenge: "The Real-Time Payment Gateway"

**Scenario:** You are building the public-facing API for an Instant Transfer service. You need to expose a REST endpoint that accepts a transfer request, validates it against multiple internal services, and executes it on a legacy core banking system.

## The Endpoint
`POST /api/v1/payments`

### The Request Body

```json
{
  "userId": "user_123",
  "amount": 500.00,
  "targetAccount": "acc_999"
}
```

## The Logic Flow

1.  **Pre-Check:** Verify the user exists.
2.  **Parallel Risk & Funds:**
    *   **Risk Check:** Call `RiskService`. If risk is "HIGH", reject immediately.
    *   **Funds Check:** Call `LedgerService`. If false (insufficient funds), reject.
    *   *Constraint:* These must run in parallel to minimize latency.
3.  **Execution:** If all checks pass, save the transaction to the `LegacyBankingSystem` (Blocking JDBC).

## Response

*   **Success:** HTTP 200 + JSON `{ "status": "ACCEPTED", "txId": "..." }`
*   **Risk Failure:** HTTP 403 Forbidden.
*   **No Funds:** HTTP 422 Unprocessable Entity.
*   **User Not Found:** HTTP 404 Not Found.

## The Mocks (Copy these to start)

```java
// Domain Objects
record PaymentRequest(String userId, BigDecimal amount, String targetAccount) {}
record PaymentResponse(String txId, String status) {}
enum RiskLevel { LOW, MEDIUM, HIGH }

// Services
Mono<Boolean> userExists(String userId); // Async
Mono<RiskLevel> checkRisk(String userId); // Async, Slow (need timeout)
Mono<Boolean> hasSufficientFunds(String userId, BigDecimal amount); // Async
PaymentResponse executeTransfer(PaymentRequest request); // BLOCKING I/O
```

## Your Task

Create the `PaymentController` and the `PaymentService`.

### Constraints

*   The Controller method must return `Mono<ResponseEntity<PaymentResponse>>`.
*   The `RiskService` is flaky: Timeout after 500ms and fallback to `RiskLevel.HIGH` (Safety first: if we can't check risk, assume it's risky).
*   Handle the Blocking I/O correctly.
*   Map the business errors (Funds/Risk) to the correct HTTP Status Codes.

**Go! Build the project structure as you see fit.**