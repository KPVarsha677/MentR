MentR - Smart Mentor-Mentee Management Platform
A College Semester Project

===========================================================
QUICK START GUIDE
===========================================================

PREREQUISITES:
  - Java 17
  - Maven 3.8+
  - Node.js 18+
  - MySQL 8.0+
  - Git

===========================================================
STEP 1: DATABASE SETUP
===========================================================

1. Start your local MySQL server (port 3306).
2. You do NOT need to manually create the database — the backend connects
   with createDatabaseIfNotExist=true and will create 'mentr_db' automatically,
   and Hibernate will auto-create the tables on first start.
3. (Optional) Run schema.sql for sample data instead of a clean start:
   mysql -u root -p mentr_db < database/schema.sql

===========================================================
STEP 2: CONFIGURE DATABASE + JWT SECRET (ENVIRONMENT VARIABLES)
===========================================================

The database password and JWT signing secret are NOT stored in
application.properties (never commit real secrets to source control).
They are read from the environment variables DB_PASSWORD and JWT_SECRET.

Option A — set OS environment variables before running the backend:
  Windows (PowerShell):
    $env:DB_PASSWORD = "your_mysql_password"
    $env:JWT_SECRET   = "any-long-random-string-at-least-64-characters"
  macOS/Linux:
    export DB_PASSWORD=your_mysql_password
    export JWT_SECRET=any-long-random-string-at-least-64-characters

Option B (used for local development on this machine) — create a file
backend/config/application.properties (this path is gitignored and will
never be committed) containing:
    DB_PASSWORD=your_mysql_password
    JWT_SECRET=any-long-random-string-at-least-64-characters
Spring Boot automatically loads this file with higher priority than the
classpath application.properties, so no other configuration is needed —
just run mvn spring-boot:run from the backend folder as usual.

===========================================================
STEP 3: START THE BACKEND
===========================================================

Open terminal in the 'backend' folder:

  cd mentorhub/backend
  mvn spring-boot:run

Backend will start at: http://localhost:8081

You should see:
  ========================================
  MentR Backend Started!
  API running at: http://localhost:8081
  ========================================

===========================================================
STEP 4: INSTALL AND START THE FRONTEND
===========================================================

Open a NEW terminal in the 'frontend' folder:

  cd mentorhub/frontend
  npm install
  npm start

Frontend will start at: http://localhost:3000

Your browser will automatically open the app.

===========================================================
STEP 5: TEST THE APPLICATION
===========================================================

1. Open http://localhost:3000
2. Click "Register here"

TEST TEACHER ACCOUNT:
  Name:     Dr. John Smith
  Email:    john@mentorhub.com
  Password: password123
  Role:     Teacher

TEST STUDENT ACCOUNT:
  Name:     Alice Doe
  Email:    alice@mentorhub.com
  Password: password123
  Role:     Student

After registering both accounts:
  - Teacher: Go to Classrooms → Create a classroom → Note the join code
  - Student: Go to Classrooms → Enter the join code → Join
  - Student: Go to Projects → Add a project
  - Teacher: Go to Dashboard → See the notification
  - Teacher: Go to Students (from classroom) → View Alice's profile → Verify the project

===========================================================
PROJECT STRUCTURE
===========================================================

mentorhub/
├── backend/
│   ├── pom.xml                          ← Maven dependencies
│   └── src/main/java/com/mentorhub/
│       ├── MentRApplication.java    ← App entry point
│       ├── config/
│       │   └── SecurityConfig.java      ← Spring Security configuration
│       ├── controller/
│       │   ├── AuthController.java      ← Login & Register APIs
│       │   ├── StudentController.java   ← Student profile CRUD APIs
│       │   ├── TeacherController.java   ← Teacher dashboard APIs
│       │   ├── ClassroomController.java ← Classroom management APIs
│       │   └── ReportController.java    ← Report generation APIs
│       ├── service/
│       │   ├── AuthService.java         ← Login & Register logic
│       │   ├── StudentService.java      ← Student profile operations
│       │   ├── TeacherService.java      ← Teacher dashboard operations
│       │   ├── ClassroomService.java    ← Classroom operations
│       │   └── NotificationService.java ← Notification creation & fetching
│       ├── entity/
│       │   ├── User.java               ← User entity (mapped to 'users' table)
│       │   ├── StudentProfile.java     ← Student profile entity
│       │   ├── TeacherProfile.java     ← Teacher profile entity
│       │   ├── Classroom.java          ← Classroom entity
│       │   ├── ClassroomMember.java    ← Join table entity
│       │   ├── Project.java            ← Project entity
│       │   ├── Skill.java              ← Skill entity
│       │   ├── Certification.java      ← Certification entity
│       │   ├── Internship.java         ← Internship entity
│       │   ├── Achievement.java        ← Achievement entity
│       │   └── Notification.java       ← Notification entity
│       ├── repository/                 ← Spring Data JPA interfaces
│       ├── dto/                        ← Request/Response data transfer objects
│       ├── security/
│       │   ├── JwtUtil.java            ← JWT create/validate
│       │   ├── JwtAuthFilter.java      ← JWT request filter
│       │   └── CustomUserDetailsService.java ← Load user for Spring Security
│       └── util/
│           └── GlobalExceptionHandler.java ← Centralized error handling
│
├── frontend/
│   └── src/
│       ├── App.js                      ← Route definitions
│       ├── services/
│       │   └── api.js                  ← Axios configuration with JWT interceptor
│       ├── components/
│       │   └── ProtectedRoute.js       ← Guards routes from unauthorized access
│       ├── layouts/
│       │   ├── StudentLayout.js        ← Sidebar for student pages
│       │   └── TeacherLayout.js        ← Sidebar for teacher pages
│       └── pages/
│           ├── LoginPage.js            ← Login form
│           ├── RegisterPage.js         ← Registration form
│           ├── student/
│           │   ├── StudentDashboard.js ← Student home with stats
│           │   ├── StudentProfile.js   ← Personal/academic info form
│           │   ├── StudentProjects.js  ← Project CRUD with status
│           │   ├── StudentSkills.js    ← Skill CRUD with status
│           │   ├── StudentCertifications.js ← Cert CRUD with status
│           │   ├── StudentInternships.js    ← Internship CRUD with status
│           │   ├── StudentAchievements.js   ← Achievement CRUD with status
│           │   └── StudentClassrooms.js     ← Join/leave classrooms
│           └── teacher/
│               ├── TeacherDashboard.js    ← Stats + recent notifications
│               ├── TeacherClassrooms.js   ← Create/manage classrooms
│               ├── TeacherStudentView.js  ← View + verify student portfolio
│               ├── TeacherSearch.js       ← Search students by filters
│               ├── TeacherNotifications.js← Notification list
│               └── TeacherReports.js      ← Report generation
│
└── database/
    └── schema.sql                      ← Manual database schema

===========================================================
API ENDPOINTS SUMMARY
===========================================================

AUTH:
  POST /api/auth/register   - Register new user
  POST /api/auth/login      - Login and get JWT

STUDENT (requires ROLE_STUDENT JWT):
  GET  /api/student/{id}/profile      - Get profile
  PUT  /api/student/{id}/profile      - Update profile
  GET/POST /api/student/{id}/projects - Get/Add projects
  PUT/DELETE /api/student/{id}/projects/{pid} - Edit/Delete project
  (Same pattern for skills, certifications, internships, achievements)

TEACHER (requires ROLE_TEACHER JWT):
  GET  /api/teacher/{id}/dashboard    - Dashboard stats
  GET  /api/teacher/{id}/students/search - Search students
  GET  /api/teacher/students/{sid}/profile - View student profile
  POST /api/teacher/verify/project/{id}   - Verify project
  (Same pattern for certifications, internships, achievements, skills)
  GET  /api/teacher/{id}/notifications    - Get notifications
  PUT  /api/teacher/{id}/notifications/read - Mark as read

CLASSROOMS:
  POST /api/classrooms              - Create classroom
  GET  /api/classrooms/teacher/{id} - Teacher's classrooms
  POST /api/classrooms/join         - Student joins classroom
  GET  /api/classrooms/student/{id} - Student's classrooms

REPORTS (requires ROLE_TEACHER JWT):
  GET /api/reports/student/{id}/portfolio - Full portfolio
  GET /api/reports/classroom/{id}/summary - Classroom summary
  GET /api/reports/pending-verifications  - All pending items

===========================================================
COMMON ERRORS AND FIXES
===========================================================

ERROR: "Access Denied" or 403 Forbidden
FIX: Check that the JWT token is being sent in the Authorization header.
     Make sure the user has the correct role for the endpoint.

ERROR: "Could not connect to MySQL"
FIX: Ensure MySQL is running and that DB_PASSWORD is set correctly
     (see STEP 2). The database name is 'mentr_db' — it is created
     automatically if it doesn't exist.

ERROR: CORS error in browser console
FIX: Make sure both frontend (port 3000) and backend (port 8081) are running.
     The CORS config in SecurityConfig.java allows http://localhost:3000.

ERROR: "User not found" after login
FIX: Try registering again. If the error is in a service method,
     make sure the userId in localStorage matches a real user in the DB.

ERROR: "npm install" fails
FIX: Delete node_modules folder and run npm install again.
     Make sure Node.js version is 18 or higher.

ERROR: Port 8081 already in use
FIX: Change server.port in application.properties, or kill the process:
     Windows: netstat -ano | findstr :8081 → taskkill /PID <pid> /F

===========================================================
VIVA PREPARATION - KEY QUESTIONS
===========================================================

Q: What is JWT and why did you use it?
A: JWT (JSON Web Token) is a way to prove identity without keeping sessions on the server.
   After login, we give the user a signed token. They send it with every request.
   We validate the signature — if valid, we know who they are.
   We used it because it is stateless, simple, and works perfectly with REST APIs.

Q: What is BCrypt?
A: BCrypt is a password hashing algorithm. It converts a plain text password
   like "mypass123" into a hash like "$2a$10$....". Even if the database is stolen,
   the attacker cannot easily reverse-engineer the original password.

Q: What is a DTO?
A: DTO = Data Transfer Object. It is a simple class that carries data between
   the frontend and backend. We use DTOs instead of exposing entity classes
   directly because entities have database annotations and may expose
   internal database structure.

Q: Explain the Verification Flow.
A: 1. Student adds a project → verificationStatus = "PENDING"
   2. A Notification is created for the teacher
   3. Teacher opens the student's profile
   4. Teacher clicks Verify → selects APPROVED or REJECTED + optional comment
   5. Student can see the updated status and comment on their profile page.

Q: How do classrooms work?
A: A teacher creates a classroom. The system generates a unique 8-character code.
   The teacher shares the code with students.
   Students enter the code on their Classrooms page.
   The system creates a ClassroomMember record linking them.
   Teachers can then see students in their classroom and get notified about their activities.

Q: What is Spring Security?
A: Spring Security is a framework that handles authentication and authorization.
   Authentication = Who are you? (login with email+password)
   Authorization = What can you do? (teacher can verify, student cannot)
   We configured it to use JWT tokens and role-based access control.
