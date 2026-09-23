package com.mentorhub.repository;

import com.mentorhub.entity.StudentProfile;
import com.mentorhub.entity.User;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-level test for {@link StudentProfileRepository#searchStudents} run against
 * a REAL, embedded PostgreSQL instance — not H2.
 *
 * WHY A REAL POSTGRES INSTANCE IS REQUIRED FOR THIS TEST:
 * The production bug reported for GET /api/teacher/{id}/students/search (and reused by
 * the Reports page's register-number lookup) is caused by how Hibernate 6 translates the
 * repository's hand-written JPQL:
 *
 *   (:registerNumber IS NULL OR LOWER(sp.registerNumber) LIKE LOWER(CONCAT('%', :registerNumber, '%')))
 *
 * Each named parameter here is written ONCE in JPQL but is used TWICE — once in the
 * "IS NULL" check and once inside CONCAT(...). Hibernate's SQL translator emits a
 * SEPARATE native bind position for each occurrence. PostgreSQL's extended query
 * protocol tries to infer each bind position's type independently; the "IS NULL"
 * occurrence carries no type information on its own (NULL could be any type), and
 * PostgreSQL fails to resolve it, throwing at query time:
 *
 *   org.postgresql.util.PSQLException: ERROR: could not determine data type of parameter $N
 *
 * H2, even in PostgreSQL-compatibility mode, does NOT reproduce this — it doesn't use
 * PostgreSQL's actual wire protocol/type-inference rules, so a query broken this way
 * against real PostgreSQL passes silently under H2. That's why this test spins up a
 * throwaway embedded PostgreSQL instance instead: it's the only way to trust the result.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentProfileRepositorySearchTest {

    private static EmbeddedPostgres pg;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) throws IOException {
        pg = EmbeddedPostgres.builder().start();
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:" + pg.getPort() + "/postgres");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @AfterAll
    static void shutdown() throws IOException {
        if (pg != null) {
            pg.close();
        }
    }

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    private StudentProfile seedStudent(String name, String email, String registerNumber,
                                        String department, Integer year, String section) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("hashed-not-relevant");
        u.setRole("ROLE_STUDENT");
        u = userRepository.save(u);

        StudentProfile sp = new StudentProfile();
        sp.setUser(u);
        sp.setRegisterNumber(registerNumber);
        sp.setDepartment(department);
        sp.setYear(year);
        sp.setSection(section);
        sp.setBatch("2024-2028");
        return studentProfileRepository.save(sp);
    }

    @Test
    void searchByRegisterNumberOnly_matchesAgainstRealPostgres() {
        // This is exactly the call TeacherController#searchStudents makes when a teacher
        // enters only a register number: name/department/section/year are all null.
        seedStudent("Varsha K Pavithran", "varsha@example.edu", "CSE24A000", "CSE", 2, "A");

        List<StudentProfile> results =
                studentProfileRepository.searchStudents(null, "CSE24A000", null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRegisterNumber()).isEqualTo("CSE24A000");
    }

    @Test
    void searchByNameOnly_matchesAgainstRealPostgres() {
        seedStudent("Varsha K Pavithran", "varsha2@example.edu", "CSE24A001", "CSE", 2, "A");

        List<StudentProfile> results =
                studentProfileRepository.searchStudents("Varsha", null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUser().getName()).isEqualTo("Varsha K Pavithran");
    }

    @Test
    void searchWithNoFilters_returnsAllStudents() {
        seedStudent("Student A", "a@example.edu", "CSE24A002", "CSE", 2, "A");
        seedStudent("Student B", "b@example.edu", "CSE24A003", "CSE", 2, "B");

        List<StudentProfile> results =
                studentProfileRepository.searchStudents(null, null, null, null, null);

        assertThat(results).hasSize(2);
    }
}
