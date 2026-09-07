-- ============================================================
-- MentR Database Setup Script
-- Run this if you want to create the database manually.
-- Otherwise, Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- will auto-create all tables from entity classes.
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS mentr_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mentr_db;

-- ============================================================
-- TABLE: users
-- Stores login credentials for all users (teachers + students)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,    -- 'ROLE_TEACHER' or 'ROLE_STUDENT'
    profile_picture VARCHAR(255),
    bio             TEXT,
    phone           VARCHAR(20),
    created_at      DATETIME     NOT NULL
);

-- ============================================================
-- TABLE: student_profiles
-- Stores academic and personal info for students
-- ============================================================
CREATE TABLE IF NOT EXISTS student_profiles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNIQUE NOT NULL,
    register_number VARCHAR(50),
    department      VARCHAR(100),
    year            INT,
    section         VARCHAR(10),
    batch           VARCHAR(20),
    phone           VARCHAR(20),
    address         TEXT,
    linkedin_url    VARCHAR(255),
    github_url      VARCHAR(255),
    career_goal     TEXT,
    about           TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: teacher_profiles
-- Stores professional info for teachers
-- ============================================================
CREATE TABLE IF NOT EXISTS teacher_profiles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNIQUE NOT NULL,
    department  VARCHAR(100),
    employee_id VARCHAR(50),
    phone       VARCHAR(20),
    designation VARCHAR(100),
    subject     VARCHAR(100),
    about       TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: classrooms
-- Teacher-created virtual classrooms
-- ============================================================
CREATE TABLE IF NOT EXISTS classrooms (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    description   TEXT,
    subject       VARCHAR(100),
    academic_year VARCHAR(20),
    teacher_id    BIGINT       NOT NULL,
    join_code     VARCHAR(20)  NOT NULL UNIQUE,
    active        BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: classroom_members
-- Tracks which students have joined which classrooms
-- ============================================================
CREATE TABLE IF NOT EXISTS classroom_members (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT   NOT NULL,
    student_id   BIGINT   NOT NULL,
    joined_at    DATETIME NOT NULL,
    UNIQUE KEY uq_classroom_student (classroom_id, student_id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)   REFERENCES users(id)      ON DELETE CASCADE
);

-- ============================================================
-- TABLE: projects
-- Student projects with verification status
-- ============================================================
CREATE TABLE IF NOT EXISTS projects (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT       NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    tech_stack          VARCHAR(255),
    project_url         VARCHAR(500),
    start_date          VARCHAR(20),
    end_date            VARCHAR(20),
    verification_status VARCHAR(20)  DEFAULT 'PENDING',
    teacher_comment     TEXT,
    verified_at         DATETIME,
    verified_by         BIGINT,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS skills (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT      NOT NULL,
    name                VARCHAR(100) NOT NULL,
    category            VARCHAR(50),
    proficiency_level   VARCHAR(50),
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    teacher_comment     TEXT,
    verified_at         DATETIME,
    verified_by         BIGINT,
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: certifications
-- Student certifications with verification status
-- ============================================================
CREATE TABLE IF NOT EXISTS certifications (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id           BIGINT       NOT NULL,
    name                 VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(200),
    issue_date           VARCHAR(20),
    expiration_date      VARCHAR(20),
    credential_url       VARCHAR(500),
    credential_id        VARCHAR(100),
    verification_status  VARCHAR(20) DEFAULT 'PENDING',
    teacher_comment      TEXT,
    verified_at          DATETIME,
    verified_by          BIGINT,
    created_at           DATETIME    NOT NULL,
    updated_at           DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: internships
-- Student internship records with verification status
-- ============================================================
CREATE TABLE IF NOT EXISTS internships (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id          BIGINT       NOT NULL,
    company_name        VARCHAR(200) NOT NULL,
    role                VARCHAR(150),
    description         TEXT,
    start_date          VARCHAR(20),
    end_date            VARCHAR(20),
    ongoing             BOOLEAN     DEFAULT FALSE,
    location            VARCHAR(200),
    stipend             VARCHAR(50),
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    teacher_comment     TEXT,
    verified_at         DATETIME,
    verified_by         BIGINT,
    created_at          DATETIME    NOT NULL,
    updated_at          DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: achievements
-- Student awards, honors, competition results
-- ============================================================
CREATE TABLE IF NOT EXISTS achievements (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id           BIGINT       NOT NULL,
    title                VARCHAR(200) NOT NULL,
    category             VARCHAR(50),
    description          TEXT,
    achievement_date     VARCHAR(20),
    issuing_organization VARCHAR(200),
    verification_status  VARCHAR(20) DEFAULT 'PENDING',
    teacher_comment      TEXT,
    verified_at          DATETIME,
    verified_by          BIGINT,
    created_at           DATETIME    NOT NULL,
    updated_at           DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: notifications
-- Notifications sent to teachers when students update their profile
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT       NOT NULL,
    student_id BIGINT       NOT NULL,
    message    VARCHAR(500) NOT NULL,
    item_type  VARCHAR(50),
    action     VARCHAR(20),
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: academic_records
-- Per-subject marks for each student across 8 semesters
-- ============================================================
CREATE TABLE IF NOT EXISTS academic_records (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    BIGINT       NOT NULL,
    semester      INT          NOT NULL,
    subject_name  VARCHAR(200) NOT NULL,
    subject_code  VARCHAR(50)  NOT NULL,
    unit_test1    INT,
    unit_test2    INT,
    cat1          INT,
    cat2          INT,
    semester_exam INT,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- Sample data for testing
-- ============================================================

-- Insert a teacher (password: teacher123)
INSERT IGNORE INTO users (name, email, password, role, created_at)
VALUES ('Dr. Sarah Johnson', 'teacher@mentorhub.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lheO',
        'ROLE_TEACHER', NOW());

-- Insert teacher profile
INSERT IGNORE INTO teacher_profiles (user_id, department, employee_id, designation)
VALUES (LAST_INSERT_ID(), 'Computer Science', 'TC001', 'Assistant Professor');

-- Note: The password hash above is for "teacher123"
-- Use bcrypt to generate proper hashes in production
