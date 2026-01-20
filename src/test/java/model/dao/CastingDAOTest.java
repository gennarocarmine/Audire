package model.dao;

import model.dto.CastingDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CastingDAOTest {

    private static CastingDAO castingDAO;
    private static JdbcDataSource dataSource;

    // Static IDs for dependencies
    private static int savedPmID = 0;
    private static int savedCdID = 0;
    private static int savedProductionID = 0;
    private static int savedCastingID = 0;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb_casting;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        castingDAO = new CastingDAO(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE User (" +
                    "UserID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "FirstName VARCHAR(50), LastName VARCHAR(50), PasswordHash VARCHAR(255), " +
                    "PhoneNumber CHAR(10), Role VARCHAR(20), Email VARCHAR(100) UNIQUE, RegistrationDate DATETIME)");

            stmt.execute("CREATE TABLE Production_Manager (PmID INT PRIMARY KEY AUTO_INCREMENT, UserID INT, FOREIGN KEY(UserID) REFERENCES User(UserID))");

            stmt.execute("CREATE TABLE Casting_Director (CdID INT PRIMARY KEY AUTO_INCREMENT, UserID INT, FOREIGN KEY(UserID) REFERENCES User(UserID))");

            stmt.execute("CREATE TABLE Production (" +
                    "ProductionID INT PRIMARY KEY AUTO_INCREMENT, Title VARCHAR(255), " +
                    "Type VARCHAR(50), CreationDate DATETIME, PmID INT, " +
                    "FOREIGN KEY(PmID) REFERENCES Production_Manager(PmID))");

            stmt.execute("CREATE TABLE Casting (" +
                    "CastingID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "Location VARCHAR(255) NOT NULL, " +
                    "Category ENUM('Attore/Attrice', 'Musicista', 'Cantante', 'Ballerino', 'Doppiatore/trice', 'Qualsiasi') NOT NULL, " +
                    "Description TEXT NOT NULL, " +
                    "PublishDate DATETIME NOT NULL, " +
                    "DeadLine DATETIME NOT NULL, " +
                    "Title VARCHAR(255) NOT NULL, " +
                    "CdID INT NOT NULL, " +
                    "ProductionID INT NOT NULL, " +
                    "FOREIGN KEY (CdID) REFERENCES Casting_Director(CdID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ProductionID) REFERENCES Production(ProductionID) ON DELETE CASCADE" +
                    ");");


            stmt.execute("INSERT INTO User (FirstName, LastName, Role, Email) VALUES ('PM', 'User', 'ProductionManager', 'pm@test.com')");
            stmt.execute("INSERT INTO Production_Manager (UserID) VALUES (LAST_INSERT_ID())");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) savedPmID = rs.getInt(1); }

            stmt.execute("INSERT INTO User (FirstName, LastName, Role, Email) VALUES ('CD', 'User', 'CastingDirector', 'cd@test.com')");
            stmt.execute("INSERT INTO Casting_Director (UserID) VALUES (LAST_INSERT_ID())");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) savedCdID = rs.getInt(1); }

            stmt.execute("INSERT INTO Production (Title, Type, CreationDate, PmID) VALUES ('Test Production', 'Film', NOW(), " + savedPmID + ")");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) savedProductionID = rs.getInt(1); }
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE Casting");
            stmt.execute("DROP TABLE Production");
            stmt.execute("DROP TABLE Casting_Director");
            stmt.execute("DROP TABLE Production_Manager");
            stmt.execute("DROP TABLE User");
        }
    }

    @Test
    @Order(1)
    void testSaveInsert() throws SQLException {
        CastingDTO casting = new CastingDTO();
        casting.setTitle("Ricerca Protagonista");
        casting.setLocation("Roma");
        casting.setCategory(CastingDTO.Category.Attore_Attrice);
        casting.setDescription("Descrizione ruolo principale...");
        casting.setPublishDate(LocalDateTime.now());
        casting.setDeadline(LocalDateTime.now().plusDays(30)); // Future deadline

        // Link to dependencies
        casting.setCdID(savedCdID);
        casting.setProductionID(savedProductionID);

        castingDAO.save(casting);

        assertTrue(casting.getCastingID() > 0, "CastingID should be generated by the database.");
        savedCastingID = casting.getCastingID();

        CastingDTO retrieved = castingDAO.getByID(savedCastingID);
        assertNotNull(retrieved);
        assertEquals("Roma", retrieved.getLocation());
        assertEquals(CastingDTO.Category.Attore_Attrice, retrieved.getCategory(), "Enum mapping should work (Attore/Attrice)");
    }

    @Test
    @Order(2)
    void testGetByID() throws SQLException {
        CastingDTO c = castingDAO.getByID(savedCastingID);
        assertNotNull(c);
        assertEquals(savedCdID, c.getCdID());
        assertEquals(savedProductionID, c.getProductionID());

        assertNull(castingDAO.getByID(9999), "Should return null for non-existent ID");
    }

    @Test
    @Order(3)
    void testSaveUpdate() throws SQLException {
        CastingDTO c = castingDAO.getByID(savedCastingID);

        // Update fields
        c.setTitle("Nuovo Titolo Casting");
        c.setLocation("Milano");
        c.setCategory(CastingDTO.Category.Ballerino);

        castingDAO.save(c);

        CastingDTO updated = castingDAO.getByID(savedCastingID);
        assertEquals("Nuovo Titolo Casting", updated.getTitle());
        assertEquals("Milano", updated.getLocation());
        assertEquals(CastingDTO.Category.Ballerino, updated.getCategory());
    }

    @Test
    @Order(4)
    void testGetByProductionID() throws SQLException {
        Collection<CastingDTO> list = castingDAO.getByProductionID(savedProductionID);
        assertFalse(list.isEmpty());
        assertEquals(savedProductionID, list.iterator().next().getProductionID());

        assertTrue(castingDAO.getByProductionID(9999).isEmpty());
    }

    @Test
    @Order(5)
    void testGetByCdID() throws SQLException {
        Collection<CastingDTO> list = castingDAO.getByCdID(savedCdID);
        assertFalse(list.isEmpty());
        assertEquals(savedCdID, list.iterator().next().getCdID());

        assertTrue(castingDAO.getByCdID(9999).isEmpty());
    }

    @Test
    @Order(6)
    void testGetAllActive() throws SQLException {
        // Create an EXPIRED casting
        CastingDTO expired = new CastingDTO();
        expired.setTitle("Expired Casting");
        expired.setLocation("Napoli");
        expired.setCategory(CastingDTO.Category.Musicista);
        expired.setDescription("Old...");
        expired.setPublishDate(LocalDateTime.now().minusDays(20));
        expired.setDeadline(LocalDateTime.now().minusDays(1)); // Yesterday
        expired.setCdID(savedCdID);
        expired.setProductionID(savedProductionID);
        castingDAO.save(expired);

        List<CastingDTO> activeList = castingDAO.getAllActive();

        boolean containsFuture = activeList.stream().anyMatch(c -> c.getCastingID() == savedCastingID);
        boolean containsExpired = activeList.stream().anyMatch(c -> c.getCastingID() == expired.getCastingID());

        assertTrue(containsFuture, "Should contain the casting with future deadline");
        assertFalse(containsExpired, "Should NOT contain the casting with past deadline");
    }

    @Test
    @Order(7)
    void testDelete() throws SQLException {
        boolean deleted = castingDAO.delete(savedCastingID);
        assertTrue(deleted);
        assertNull(castingDAO.getByID(savedCastingID));
    }
}