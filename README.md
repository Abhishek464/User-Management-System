# User-Management-System

User Management System

A Spring Boot–based User Management System providing:

User registration and login

JWT-based authentication and authorization

Role-based access control (RBAC)

MySQL persistence using Spring Data JPA

Asynchronous user events via RabbitMQ (user registration, user login)

The project is runnable both locally and via Docker Compose.

Tech Stack

Language: Java 17

Framework: Spring Boot 3.2

Modules:

spring-boot-starter-web – REST API

spring-boot-starter-security – authentication & authorization

spring-boot-starter-data-jpa – persistence

spring-boot-starter-validation – request validation

spring-boot-starter-amqp – RabbitMQ integration

jjwt – JSON Web Tokens (JWT)

Database: MySQL 8

Messaging: RabbitMQ

Build Tool: Maven

Containerization: Docker + Docker Compose

Getting Started (Local Setup)
1. Prerequisites

Make sure you have:

Java 17 or higher

Maven (or use the provided mvnw / mvnw.cmd)

MySQL 8 installed and running on localhost:3306

RabbitMQ (optional but recommended for event publishing)

If you don’t want to install RabbitMQ locally, you can run only RabbitMQ via Docker using the provided compose.yaml.

2. Clone the Repository
git clone https://github.com/Abhishek464/User-Management-System.git
cd User-Management-System

3. Local Database Setup

The application is configured (for local) with the following properties in
src/main/resources/application.properties:

spring.application.name=demo
spring.docker.compose.enabled=false

# JWT configuration
jwt.secret=YOUR_LONG_RANDOM_SECRET_HERE
jwt.expiration-ms=3600000

# RabbitMQ event queues
app.events.user-registration-queue=user-registration-queue
app.events.user-login-queue=user-login-queue

# Local MySQL configuration
spring.datasource.url=jdbc:mysql://localhost:3306/UserDatabase
spring.datasource.username=root
spring.datasource.password=REPLACE_WITH_YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

3.1 Create the database

Login to MySQL and create the database:

CREATE DATABASE UserDatabase;


If you want to use a dedicated DB user instead of root, you can do:

CREATE USER 'user'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON UserDatabase.* TO 'user'@'%';
FLUSH PRIVILEGES;


And then update your application.properties:

spring.datasource.username=user
spring.datasource.password=password


Schema Handling
There are no manual SQL migrations in this project.
Spring JPA creates/updates tables automatically via:

spring.jpa.hibernate.ddl-auto=update


The actual tables are generated from JPA entities (e.g., User, Role, and mapping tables).

4. Configure JWT Secret

Set a strong secret key in application.properties:

jwt.secret=f6c500db1756af83ea178aef3aab2c64
jwt.expiration-ms=3600000   # 1 hour in milliseconds


For production, you should override this via environment variables or external config.

5. RabbitMQ (Local)

You have two options:

Option A: Native RabbitMQ Install

Install RabbitMQ and run it locally (defaults):

Host: localhost

Port: 5672

Management UI: http://localhost:15672 (user: guest, password: guest)

Option B: Run via Docker (using compose)

From project root:

docker compose up rabbitmq


The provided compose.yaml exposes:

5672:5672 – AMQP port

15672:15672 – Management UI port

The application uses these queues:

user-registration-queue

user-login-queue

6. Run the Application Locally

From the project root:

Using Maven wrapper:

./mvnw spring-boot:run


Or with Maven:

mvn spring-boot:run


The API will be available at:

http://localhost:8080


Example endpoints (based on code & usage):

POST /api/users/register – register a new user

POST /api/auth/login – authenticate and get JWT

Secured endpoints – require Authorization: Bearer <token>

Docker-Based Setup (Full Stack: App + MySQL + RabbitMQ)

This project includes a compose.yaml that brings up:

MySQL (mysql:8.0)

RabbitMQ (rabbitmq:3-management)

The User Management application (built from this project)

Key parts of compose.yaml (simplified):

version: "3.9"

services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: userdb
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"

  app:
    build: .
    container_name: user-rbac-app
    depends_on:
      mysql:
        condition: service_healthy
      rabbitmq:
        condition: service_started
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/userdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: user
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_PORT: 5672
    ports:
      - "8080:8080"

volumes:
  mysql_data:


Note: For the Dockerized app, DB and RabbitMQ configurations are injected via environment variables and override the local application.properties settings.

Steps to Run with Docker

From the project root:

docker compose up --build


This will:

Build the Spring Boot app image

Start MySQL, RabbitMQ, and the app

Expose the app at http://localhost:8080

You can then hit the same endpoints as in local setup.

To stop:

docker compose down

Database Schema (Conceptual Overview)

The schema is driven by JPA entities. Hibernate creates/updates tables automatically (no manual SQL migrations in this repo).

Conceptually, the main tables are:

users

id (PK, auto-generated)

username (unique)

email (unique)

password (hashed via Spring Security PasswordEncoder)

enabled / status flags

created_at, updated_at

roles

id (PK)

name (e.g., ROLE_USER, ROLE_ADMIN)

user_roles (junction table for many-to-many)

user_id (FK → users.id)

role_id (FK → roles.id)

Event/Audit-related tables (if defined in entities)

For storing login/registration logs or other domain events, depending on entities.

Because spring.jpa.hibernate.ddl-auto=update, the exact DDL can vary slightly by MySQL version, but the above captures the logical model.

Design Decisions & Assumptions
1. Spring Boot + Layered Architecture

Reason: Spring Boot 3.2 offers opinionated auto-configuration, good integration with Spring Security, JPA, and AMQP.

Likely layers:

Controller – REST endpoints (/api/...)

Service – business logic (user registration, authentication, role assignment)

Repository – data access via Spring Data JPA

Security – filters, JWT handling, access control

Messaging – RabbitMQ producers for user events

2. JWT-Based Authentication (Stateless)

Reason: JWT allows the API to remain stateless, which is simpler to scale horizontally.

Typical flow:

User registers (/api/users/register)

User logs in (/api/auth/login)

System issues a JWT token

Subsequent requests include Authorization: Bearer <token>

Assumption: JWT is signed with HS256 using jwt.secret; tokens are short-lived (jwt.expiration-ms).

3. Role-Based Access Control (RBAC)

Reason: Separating users and roles provides flexibility:

Same user can have multiple roles.

New roles can be added without schema changes (just data).

Assumption:

Roles such as ROLE_USER, ROLE_ADMIN exist.

Controller methods are protected using Spring Security annotations or configuration (e.g., hasRole('ADMIN')).

4. MySQL + JPA (DDL Auto-Update)

Reason:

MySQL is a widely used relational database, good fit for user data.

Using JPA and ddl-auto=update speeds up local development (no manual DDL needed).

Assumptions:

This project is primarily in development/demo mode.

For production, a proper migration tool (Flyway/Liquibase) should replace ddl-auto=update to maintain deterministic schema evolution.

5. RabbitMQ for User Events

Reason:

When a user registers or logs in, the system can publish events to RabbitMQ queues:

user-registration-queue

user-login-queue

This decouples the core user management from additional concerns like:

Sending welcome emails

Analytics / audit logging

Downstream integrations

Assumptions:

Consumers of these queues are separate services (not necessarily in this repo).

Events are fire-and-forget; failures in consumers do not block main API flow.

6. Docker Compose for Reproducible Environments

Reason:

compose.yaml makes it easy to bring up:

MySQL

RabbitMQ

The application

Ensures that onboarding a new developer is as simple as:

docker compose up --build

Assumptions:

Docker and Docker Compose are available on the target machine.

Host ports 8080, 3306, 5672, and 15672 are free.

7. Other Assumptions

Single-tenant system (one logical application, not multi-tenant).

username and email are treated as unique for a given user.

Passwords are stored securely via Spring Security’s PasswordEncoder (e.g. BCrypt).

Timezone handling for DB is standardized using serverTimezone=UTC in the Docker DB URL.
