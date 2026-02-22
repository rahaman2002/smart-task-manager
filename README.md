# Smart Task Manager

A modern **Single Page Application (SPA)** built with **Angular 17** and **Spring Boot**, featuring secure authentication, reactive forms, task management, RabbitMQ async notifications, and a clean glassmorphism UI.

---

## **Tech Stack**

* **Frontend:** Angular 17, TypeScript, RxJS, HTML5, CSS3 (Glassmorphism UI)
* **Backend:** Java, Spring Boot, Spring Security, JWT, JPA/Hibernate
* **Database:** PostgreSQL / MySQL (or H2 for dev)
* **Message Broker:** RabbitMQ (async notifications & task queue)
* **Build & Tooling:** Angular CLI, Maven/Gradle, Node.js, npm
* **Version Control:** Git & GitHub

---

## **Project Overview**

Smart Task Manager helps users efficiently manage tasks with secure authentication and a modern UI. Key features:

* **Secure Authentication:** JWT-based login with route guards
* **Reactive Forms:** Login and task forms with validation
* **Glassmorphism UI:** Clean and attractive interface
* **Task Management:** Create, update, delete, view tasks
* **Async Notifications:** Using RabbitMQ
* **Maintainable Architecture:** Clear frontend/backend structure for scalability

---

## **Application Flow**

1. **User Login** – Frontend sends username/password → backend validates → JWT returned
2. **JWT Token Storage** – Stored in `localStorage` → included in `Authorization` header for API requests
3. **Route Guards** – Angular prevents unauthorized access to protected routes
4. **Task Management** – CRUD operations for tasks (create, read, update, delete)
5. **Async Notifications** – Backend publishes task notifications to RabbitMQ queues
6. **Logout** – Clears JWT and redirects to login page

---

## **Setup Instructions**

### **Prerequisites**

* Java 17+
* Node.js & npm
* Docker (for PostgreSQL & RabbitMQ)

---

### **Frontend (Angular 17)**

```bash
cd frontend
npm install
ng serve
```

---

### **Backend (Spring Boot)**

```bash
mvn clean install
mvn spring-boot:run
```

---

### **Database Setup (PostgreSQL / MySQL)**

**application.properties / application.yml**

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/smarttaskdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Optional MySQL
# spring.datasource.url=jdbc:mysql://localhost:3306/smarttaskdb
# spring.datasource.username=root
# spring.datasource.password=root
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

---

### **RabbitMQ Setup (Docker)**

```bash
docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

* Management UI: [http://localhost:15672](http://localhost:15672)
* Default user/pass: `guest/guest`
* RabbitMQ is used for async notifications & task queue

---

## **Frontend Folder Structure**

```plaintext
frontend/src/
 ├─ app/
 │   ├─ login/
 │   │   ├─ login.component.ts
 │   │   └─ auth.service.ts
 │   ├─ tasks/
 │   │   ├─ task.component.ts
 │   │   └─ task.service.ts
 │   └─ app-routing.module.ts
 └─ styles.css
```

---

## **Backend Folder Structure**

```plaintext
src/main/java/org/example/smarttaskmanager/
 ├─ config/      # CORS, JWT, SecurityConfig
 ├─ controller/  # REST APIs
 ├─ model/       # Entities: User, Task, Role
 ├─ repository/  # JPA Repositories
 ├─ service/     # Business logic & RabbitMQ integration
 ├─ security/    # JWT token provider, filters
 └─ SmartTaskManagerApplication.java
```

---

## **API Endpoints**

| Endpoint           | Method | Auth | Description                     |
| ------------------ | ------ | ---- | ------------------------------- |
| /api/auth/login    | POST   | ❌    | User login, returns JWT         |
| /api/auth/register | POST   | ❌    | Register new user               |
| /api/tasks         | GET    | ✅    | Get all tasks                   |
| /api/tasks         | POST   | ✅    | Create a new task               |
| /api/tasks/{id}    | PUT    | ✅    | Update task by id               |
| /api/tasks/{id}    | DELETE | ✅    | Delete task by id               |
| /api/users/me      | GET    | ✅    | Get current logged-in user info |

> **Note:** Include JWT in `Authorization: Bearer <token>` header for all protected endpoints

---

## **JPA / Hibernate Notes**

* **Entities:**

   * `User` → stores user info, roles, last login
   * `Task` → stores task info, status, assigned user, timestamps
   * `Role` → defines `ROLE_USER` and `ROLE_ADMIN`
* **Relationships:**

   * `User` ↔ `Task` → OneToMany / ManyToOne
   * `User` ↔ `Role` → ManyToMany
* **Database Strategy:** `GenerationType.IDENTITY` for primary keys
* **Automatic Schema Update:** `spring.jpa.hibernate.ddl-auto=update`

---

## **Async Notifications Flow (RabbitMQ)**

1. When a task is created/updated → backend publishes a message to RabbitMQ queue
2. Any consumer (e.g., notification service or frontend via WebSocket) listens for updates
3. Ensures users are notified asynchronously of task changes

---

## **Tips**

* Make sure RabbitMQ and DB containers are running before starting backend
* Use Postman/curl for testing endpoints with JWT
* For dev, H2 can replace PostgreSQL/MySQL for quick testing
