# Real-Time Payment Gateway

This project implements a real-time payment gateway using Spring WebFlux. It exposes a REST endpoint to process payment requests, validating them against risk and ledger services in parallel before executing the transaction.

## Prerequisites

*   Java 21
*   Maven
*   Docker (optional, for containerization)
*   MongoDB (running locally on port 27017)

## Setup and Run

### 1. Start MongoDB
Ensure you have a MongoDB instance running locally.
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```
The application will start on port 8080.

## Testing Endpoints (cURL)

The API endpoint is `POST /api/v1/payments`.

### 1. Happy Path (Success)
**Scenario:** User exists, risk is LOW, and funds are sufficient.
*   **User ID:** Starts with `LR` (Low Risk)
*   **Amount:** Any amount (LedgerService returns true for LR)

```bash
curl -X POST http://localhost:8080/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
  "userId": "LR_12345",
  "amount": 500.00,
  "targetAccount": "acc_999"
}'
```
**Expected Response:** `200 OK` with JSON `{"txId": "...", "status": "SUCCESS"}`

### 2. Risk Failure (High Risk)
**Scenario:** RiskService identifies the user as HIGH risk.
*   **User ID:** Starts with `HR` (High Risk) - *Note: RiskService delays HR by 2s, triggering the 500ms timeout fallback to HIGH risk.*

```bash
curl -X POST http://localhost:8080/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
  "userId": "HR_00001",
  "amount": 100.00,
  "targetAccount": "acc_999"
}'
```
**Expected Response:** `403 Forbidden` with JSON `{"status": "RISK_REJECTED", ...}`

### 3. Insufficient Funds
**Scenario:** User exists, risk is acceptable, but funds are insufficient.
*   **User ID:** Does not start with `HR` or `LR` (defaults to check amount)
*   **Amount:** < 1000 (LedgerService logic: returns false if amount < 1000 for non-HR/LR users)

```bash
curl -X POST http://localhost:8080/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
  "userId": "MR_12345",
  "amount": 500.00,
  "targetAccount": "acc_999"
}'
```
**Expected Response:** `422 Unprocessable Entity` with JSON `{"status": "FUNDS_REJECTED", ...}`

### 4. User Not Found
**Scenario:** The user ID does not exist in the system.
*   **User ID:** `HR_99999` (Hardcoded in UserService to return false)

```bash
curl -X POST http://localhost:8080/api/v1/payments \
-H "Content-Type: application/json" \
-d '{
  "userId": "HR_99999",
  "amount": 500.00,
  "targetAccount": "acc_999"
}'
```
**Expected Response:** `404 Not Found`

## Dockerization

### 1. Build the Docker Image
Make sure you have built the JAR file first (`mvn clean install`).

```bash
docker build -t realtime-gateway .
```

### 2. Run with Docker
You need to link the container to your MongoDB instance.

**Option A: Using Docker Network (Recommended)**
1. Create a network:
   ```bash
   docker network create payment-net
   ```
2. Run Mongo on the network:
   ```bash
   docker run -d --name mongodb --network payment-net mongo:latest
   ```
3. Run the App on the network (overriding the mongo host):
   ```bash
   docker run -p 8080:8080 --network payment-net \
   -e SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/payment_gateway \
   realtime-gateway
   ```

**Option B: Using Host Networking (Linux/Simple Local)**
```bash
docker run -p 8080:8080 --network="host" realtime-gateway
```
*(Note: On Mac/Windows, `host` networking has limitations. Prefer Option A).*
