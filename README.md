# SunKing PAYG Backend System

## 📌 Overview

This project implements a scalable backend system for a Pay-As-You-Go (PAYG) device financing model. Customers make payments to keep their devices active. If payments are missed, devices are locked automatically.

The system is designed to handle high concurrency, asynchronous payment processing, and real-world failure scenarios.

---

## ⚙️ Tech Stack

* Java 21
* Spring Boot
* MySQL
* Kafka
* Redis
* Gradle

---

## 🚀 Setup Instructions

### 1. Clone the repository

```bash
git clone <repo-url>
cd pay-as-you-go
```

### 2. Start dependencies (Docker)

```bash
docker-compose up -d
```

### 3. Run the application

```bash
./gradlew bootRun
```

---

## 🔌 Services Used

* MySQL → Primary database
* Kafka → Asynchronous event processing
* Redis → Caching & rate limiting

---

## 📡 API Documentation
* http://localhost:7070/swagger-ui/index.html

## 🧠 Architecture Decisions

### 1. Asynchronous Payment Processing

Mobile money systems are inherently asynchronous. Payments are:

* Initiated via Kafka
* Completed via gateway callback

---

### 2. Idempotency

Each payment request includes an idempotency key to prevent duplicate processing.

---

### 3. Kafka for Decoupling

Kafka is used to:

* Decouple payment initiation
* Enable scalability via partitions

---

### 4. Redis Caching

Redis is used to cache device status for faster reads.

---

### 5. Scheduler

Used for:

* Locking devices on missed payments
* Handling stuck payments
* Reconciliation

---

## ⚖️ Trade-offs

| Decision                       | Trade-off            |
| ------------------------------ | -------------------- |
| Async payments                 | Increased complexity |
| Redis caching                  | Possible stale data  |
| Kafka usage                    | Operational overhead |


---

## 🚀 Scalability

* Kafka partitions for parallel processing
* Horizontal scaling of services
* Indexed DB queries
* Redis caching for performance

---

## 🔐 Failure Handling

* Kafka retries + DLQ
* Idempotent callbacks
* Scheduler for stuck payments
* Reconciliation job

---

## 📌 Future Improvements

* Circuit breaker for gateway
* Distributed tracing
* Monitoring (Prometheus/Grafana)
* Flyway migrations

---
