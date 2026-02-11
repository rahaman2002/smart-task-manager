# Smart Task Manager

A modern **Single Page Application (SPA)** built with **Angular 17** and **Spring Boot** backend, featuring secure authentication, reactive forms, and a clean glassmorphism UI.

---

## **Tech Stack**

- **Frontend:** Angular 17, TypeScript, RxJS, HTML5, CSS3 (Glassmorphism UI)
- **Backend:** Java, Spring Boot, Spring Security, JWT
- **Database:** PostgreSQL / MySQL (or H2 for dev)
- **Build & Tooling:** Angular CLI, Maven/Gradle, Node.js, npm
- **Version Control:** Git & GitHub

---

## **Project Overview**

Smart Task Manager is designed to help users manage tasks efficiently while ensuring secure access and a modern user experience. The app includes:

- **Secure Authentication:** JWT-based login and route guards for protected routes.
- **Reactive Forms:** For login and task management forms with validation.
- **Glassmorphism UI:** Modern, clean, and elegant UI for an attractive user experience.
- **Protected Routes:** Only authenticated users can access the main dashboard and task features.
- **Maintainable Architecture:** Scalable folder structure for both frontend and backend.

---

## **Application Flow**

1. **User Login**
   - User enters username and password in a reactive form.
   - Credentials are sent to Spring Boot backend for validation.
   - On success, backend returns a JWT token.

2. **JWT Token Storage**
   - Token is stored in localStorage/sessionStorage.
   - Used for authenticating future API requests.

3. **Route Guards**
   - Angular route guards prevent unauthorized access to protected routes.
   - Redirects unauthenticated users back to login page.

4. **Task Management**
   - Authenticated users can create, update, delete, and view tasks.
   - Data is fetched via secure REST API endpoints.

5. **Logout**
   - Clears JWT from storage.
   - Redirects to login page.

---

## **Setup Instructions**

### **Frontend (Angular 17)**
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Run Angular app
ng serve

### Backend 
# stay outside src folder
# Build and run
mvn clean install
mvn spring-boot:run

## Frontend folder structure

fronend/src/
 ├─ app/
 │   ├─ login/
 │   │   ├─ login.component.ts
 │   │   └─ auth.service.ts
 │   ├─ tasks/
 │   │   ├─ task.component.ts
 │   │   └─ task.service.ts
 │   └─ app-routing.module.ts
 └─ styles.css

## Backend folder structure
src/main/java/com/example/smarttaskmanager/
 ├─ config/      # CORS, security
 ├─ controller/  # REST APIs
 ├─ model/       # Entities
 ├─ repository/  # JPA Repositories
 ├─ security/    # JWT, Authentication
 └─ SmartTaskManagerApplication.java

