

TakeAm Platform

TakeAm is a microservices-based agricultural trading platform designed to digitize the end-to-end flow of produce trading — from trader onboarding and intake inspection to pricing, payments, and buyer marketplace interactions.

The platform is built using Java and Node.js microservices, follows modern distributed system principles, and is designed for scalability, fault tolerance, and future cloud deployment.

Project Goals

Digitize agricultural produce intake and trading

Support multiple user roles (Traders, Agents, Buyers, Admins)

Demonstrate real-world microservices architecture

Implement secure authentication and authorization

Enable event-driven communication between services

Deploy services using containers and orchestration tools

This project is also designed as a final-year academic project with strong emphasis on architecture, system design, and real-world engineering practices.

High-Level Architecture Clients (Mobile / Web) ↓ API Gateway ↓ | User Service (Java) | Intake Service (Node.js) | Pricing Engine (Java) | Inventory Service (Node.js) | Payment Service (paystack or alat pay) | Marketplace Service (Node.js)

🛠️ Technology Stack

Backend

Java (Spring Boot) – Core services, payments, pricing, ledger

Node.js (Express/NestJS) – Intake, inventory, marketplace services

Databases \& Storage

PostgreSQL – Relational data

MongoDB – Flexible domain data

Redis – Caching, OTP, sessions, rate limiting

Infrastructure

Docker – Containerization

Kubernetes – Orchestration \& scaling

Kafka – Event-driven communication

API Gateway – Central request routing

Microservices Overview

Service

Tech

Responsibility

User Service

Java

User management, roles, authentication

Intake Service

Node.js

Produce intake requests \& inspections

Pricing Engine

Java

Pricing calculations \& rules

Payment Service

Java

Payment processing \& reconciliation

Inventory Service

Node.js

Stock \& availability tracking

Marketplace Service

Node.js

Buyer-facing marketplace

API Gateway

Node / Kong

Central routing, auth, rate limiting



Authentication \& Security

OTP-based verification (Redis-backed)

JWT-based authentication

Spring Security (Java services)

Centralized authentication via API Gateway (planned)

Role-based access control (RBAC)

Communication Patterns

Synchronous: REST APIs between services

Asynchronous: Kafka events for decoupled workflows

Caching: Redis to reduce database load

Rate Limiting: Redis-backed counters

Repository Structure

takeam-platform/ ├── services/ │ ├── user-service/ # Java (Spring Boot) │ ├── intake-service/ # Node.js │ ├── pricing-engine/ # Java │ ├── payment-service/ # Java │ ├── inventory-service/ # Node.js │ └── marketplace-service/ # Node.js ├── gateway/ # API Gateway ├── infrastructure/ │ ├── docker/ │ ├── kubernetes/ │ └── kafka/ └── README.md

Requirements

Java 17+

Node.js 18+

Docker \& Docker Compose

Maven \& npm

General Steps

Clone repository

Start infrastructure services (PostgreSQL, Redis, Kafka)

Run individual services independently

Access services directly or via API Gateway

Microservices architecture design

Service-to-service communication

Distributed data management

Event-driven systems with Kafka

Secure authentication using JWT

Containerization and orchestration

Production-style system thinking

Author

Tega Jeremy

Final Year Project – TakeAm Platform

