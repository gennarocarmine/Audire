package model.dao;

import model.dto.ApplicationDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationDAOTest {

    private static ApplicationDAO applicationDAO;
    private static JdbcDataSource dataSource;

    // Static IDs for dependencies
    private static int savedPerformerID = 0;
    private static int savedCastingID = 0;
    private static int savedApplicationID = 0;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb_app;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        applicationDAO = new ApplicationDAO(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE User (" +
                    "UserID INT PRIMARY KEY AUTO_INCREMENT, FirstName VARCHAR(50), LastName VARCHAR(50), " +
                    "PasswordHash VARCHAR(255), PhoneNumber CHAR(10), Role VARCHAR(20), Email VARCHAR(100) UNIQUE, RegistrationDate DATETIME)");

            stmt.execute("CREATE TABLE Performer (PerformerID INT PRIMARY KEY AUTO_INCREMENT, Gender VARCHAR(10), Category VARCHAR(20), Description TEXT, UserID INT, FOREIGN KEY(UserID) REFERENCES User(UserID))");
            stmt.execute("CREATE TABLE Production_Manager (PmID INT PRIMARY KEY AUTO_INCREMENT, UserID INT, FOREIGN KEY(UserID) REFERENCES User(UserID))");
            stmt.execute("CREATE TABLE Casting_Director (CdID INT PRIMARY KEY AUTO_INCREMENT, UserID INT, FOREIGN KEY(UserID) REFERENCES User(UserID))");

            stmt.execute("CREATE TABLE Production (ProductionID INT PRIMARY KEY AUTO_INCREMENT, Title VARCHAR(255), Type VARCHAR(50), CreationDate DATETIME, PmID INT, FOREIGN KEY(PmID) REFERENCES Production_Manager(PmID))");

            stmt.execute("CREATE TABLE Casting (" +
                    "CastingID INT PRIMARY KEY AUTO_INCREMENT, Location VARCHAR(255), Category VARCHAR(20), " +
                    "Description TEXT, PublishDate DATETIME, DeadLine DATETIME, Title VARCHAR(255), " +
                    "CdID INT, ProductionID INT, " +
                    "FOREIGN KEY(CdID) REFERENCES Casting_Director(CdID), FOREIGN KEY(ProductionID) REFERENCES Production(ProductionID))");

            stmt.execute("CREATE TABLE Application (" +
                    "ApplicationID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "SendingDate DATETIME NOT NULL, " +
                    "Status ENUM('In attesa', 'Shortlist', 'Selezionata', 'Rifiutata') DEFAULT 'In attesa', " +
                    "Feedback TEXT, " +
                    "PerformerID INT NOT NULL, " +
                    "CastingID INT NOT NULL, " +
                    "FOREIGN KEY (PerformerID) REFERENCES Performer(PerformerID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (CastingID) REFERENCES Casting(CastingID) ON DELETE CASCADE" +
                    ");");


            stmt.execute("INSERT INTO User (FirstName, LastName, Email) VALUES ('Perf', 'User', 'p@test.com')");
            stmt.execute("INSERT INTO Performer (UserID) VALUES (LAST_INSERT_ID())");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) savedPerformerID = rs.getInt(1); }

            stmt.execute("INSERT INTO User (FirstName, Email) VALUES ('CD', 'cd@test.com')");
            stmt.execute("INSERT INTO Casting_Director (UserID) VALUES (LAST_INSERT_ID())"); // CdID 1

            stmt.execute("INSERT INTO User (FirstName, Email) VALUES ('PM', 'pm@test.com')");
            stmt.execute("INSERT INTO Production_Manager (UserID) VALUES (LAST_INSERT_ID())"); // PmID 1

            stmt.execute("INSERT INTO Production (Title, PmID) VALUES ('Test Prod', 1)"); // ProdID 1

            stmt.execute("INSERT INTO Casting (Title, CdID, ProductionID) VALUES ('Casting Call 1', 1, 1)");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) savedCastingID = rs.getInt(1); }
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE Application");
            stmt.execute("DROP TABLE Casting");
            stmt.execute("DROP TABLE Production");
            stmt.execute("DROP TABLE Casting_Director");
            stmt.execute("DROP TABLE Production_Manager");
            stmt.execute("DROP TABLE Performer");
            stmt.execute("DROP TABLE User");
        }
    }

    @Test
    @Order(1)
    void testSaveInsert() throws SQLException {
        ApplicationDTO app = new ApplicationDTO();
        app.setPerformerID(savedPerformerID);
        app.setCastingID(savedCastingID);
        app.setSendingDate(LocalDateTime.now());
        app.setStatus(ApplicationDTO.Status.In_attesa);
        app.setFeedback("Initial application");

        applicationDAO.save(app);

        assertTrue(app.getApplicationID() > 0, "ApplicationID should be generated");
        savedApplicationID = app.getApplicationID();

        // Verify retrieval
        ApplicationDTO retrieved = applicationDAO.getByID(savedApplicationID);
        assertNotNull(retrieved);
        assertEquals(ApplicationDTO.Status.In_attesa, retrieved.getStatus());
    }

    @Test
    @Order(2)
    void testGetByID() throws SQLException {
        ApplicationDTO app = applicationDAO.getByID(savedApplicationID);
        assertNotNull(app);
        assertEquals(savedPerformerID, app.getPerformerID());
        assertEquals(savedCastingID, app.getCastingID());

        assertNull(applicationDAO.getByID(9999), "Should return null for non-existent ID");
    }

    @Test
    @Order(3)
    void testSaveUpdate() throws SQLException {
        ApplicationDTO app = applicationDAO.getByID(savedApplicationID);
        assertNotNull(app);

        // Update status and feedback (e.g. Casting Director reviewing)
        app.setStatus(ApplicationDTO.Status.Shortlist);
        app.setFeedback("Promising candidate");

        applicationDAO.save(app);

        ApplicationDTO updated = applicationDAO.getByID(savedApplicationID);
        assertEquals(ApplicationDTO.Status.Shortlist, updated.getStatus());
        assertEquals("Promising candidate", updated.getFeedback());
    }

    @Test
    @Order(4)
    void testGetByPerformerID() throws SQLException {
        Collection<ApplicationDTO> list = applicationDAO.getByPerformerID(savedPerformerID);
        assertFalse(list.isEmpty());
        assertEquals(savedPerformerID, list.iterator().next().getPerformerID());

        assertTrue(applicationDAO.getByPerformerID(9999).isEmpty());
    }

    @Test
    @Order(5)
    void testGetByCastingID() throws SQLException {
        Collection<ApplicationDTO> list = applicationDAO.getByCastingID(savedCastingID);
        assertFalse(list.isEmpty());
        assertEquals(savedCastingID, list.iterator().next().getCastingID());

        assertTrue(applicationDAO.getByCastingID(9999).isEmpty());
    }

    @Test
    @Order(6)
    void testHasApplied() throws SQLException {
        // True case: existing application
        boolean applied = applicationDAO.hasApplied(savedPerformerID, savedCastingID);
        assertTrue(applied, "Should return true for existing application");

        // False case: non-existent application
        boolean notApplied = applicationDAO.hasApplied(savedPerformerID, 9999);
        assertFalse(notApplied, "Should return false for non-existent application");
    }

    @Test
    @Order(7)
    void testGetAll() throws SQLException {
        ApplicationDTO secondApp = new ApplicationDTO();
        secondApp.setPerformerID(savedPerformerID);
        secondApp.setCastingID(savedCastingID);
        secondApp.setSendingDate(LocalDateTime.now().minusDays(1)); // Yesterday
        secondApp.setStatus(ApplicationDTO.Status.Rifiutata);
        secondApp.setFeedback("Second Application Test");

        applicationDAO.save(secondApp);

        Collection<ApplicationDTO> allApps = applicationDAO.getAll(null);

        assertNotNull(allApps, "The list should not be null");
        assertTrue(allApps.size() >= 2, "There should be at least 2 applications in the system");

        boolean foundOriginal = allApps.stream().anyMatch(a -> a.getApplicationID() == savedApplicationID);
        boolean foundSecond = allApps.stream().anyMatch(a -> a.getApplicationID() == secondApp.getApplicationID());

        assertTrue(foundOriginal, "The original application should be in the list");
        assertTrue(foundSecond, "The second application should be in the list");
    }

    @Test
    @Order(8)
    void testDelete() throws SQLException {
        boolean deleted = applicationDAO.delete(savedApplicationID);
        assertTrue(deleted);
        assertNull(applicationDAO.getByID(savedApplicationID));
    }
}