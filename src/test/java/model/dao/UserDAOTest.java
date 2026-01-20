package model.dao;

import model.dto.UserDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static UserDAO userDAO;
    private static DataSource dataSource;
    private static UserDTO testUser;

    @BeforeAll
    static void init() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        // Configuration for H2 in MySQL mode
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        ds.setUser("sa");
        ds.setPassword("");
        dataSource = ds;
        userDAO = new UserDAO(dataSource);

        // Database Schema Setup
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement()) {
            String createTable = "CREATE TABLE User ( " +
                    "UserID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "FirstName VARCHAR(50) NOT NULL, " +
                    "LastName VARCHAR(50) NOT NULL, " +
                    "PasswordHash VARCHAR(255) NOT NULL, " +
                    "PhoneNumber CHAR(10) NOT NULL, " +
                    "Role ENUM('Performer', 'CastingDirector', 'ProductionManager') NOT NULL, " +
                    "Email VARCHAR(100) UNIQUE NOT NULL, " +
                    "RegistrationDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            stmt.execute(createTable);
        }

        // Test User Setup
        testUser = new UserDTO();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test.user@audire.it");
        testUser.setPasswordHash("pwd");
        testUser.setPhoneNumber("1234567890");
        testUser.setRole(UserDTO.Role.Performer);
        testUser.setRegistrationDate(LocalDateTime.now());
    }

    @Test
    @Order(1)
    void testSave() throws SQLException {
        userDAO.save(testUser);
        assertTrue(testUser.getUserID() > 0, "Save should generate an ID");
    }

    @Test
    @Order(2)
    void testGetByID() throws SQLException {
        UserDTO retrieved = userDAO.getByID(testUser.getUserID());
        assertNotNull(retrieved, "GetByID should return the user");
        assertEquals(testUser.getEmail(), retrieved.getEmail(), "Email should match");
    }

    @Test
    @Order(3)
    void testGetByEmail() throws SQLException {
        UserDTO retrieved = userDAO.getByEmail("test.user@audire.it");
        assertNotNull(retrieved, "GetByEmail should return the user");
        assertEquals(testUser.getUserID(), retrieved.getUserID(), "ID should match");
    }

    @Test
    @Order(4)
    void testGetAll() throws SQLException {
        UserDTO u2 = new UserDTO();
        u2.setFirstName("Two");
        u2.setLastName("Two");
        u2.setEmail("two@audire.it");
        u2.setPasswordHash("pwd");
        u2.setPhoneNumber("0987654321");
        u2.setRole(UserDTO.Role.CastingDirector);
        u2.setRegistrationDate(LocalDateTime.now());
        userDAO.save(u2);

        Collection<UserDTO> allUsers = userDAO.getAll(null);
        assertTrue(allUsers.size() >= 2, "GetAll should return at least 2 users");
    }

    @Test
    @Order(5)
    void testUpdate() throws SQLException {
        String newName = "Maria";
        testUser.setFirstName(newName);

        userDAO.save(testUser);

        UserDTO updatedUser = userDAO.getByID(testUser.getUserID());
        assertNotNull(updatedUser, "The updated user must exist");
        assertEquals(newName, updatedUser.getFirstName(), "First name should have been updated");
    }

    @Test
    @Order(6)
    void testDelete() throws SQLException {
        boolean deleted = userDAO.delete(testUser.getUserID());
        assertTrue(deleted, "Delete should return true upon success");

        UserDTO check = userDAO.getByID(testUser.getUserID());
        assertNull(check, "GetByID should return null after deletion");
    }
}