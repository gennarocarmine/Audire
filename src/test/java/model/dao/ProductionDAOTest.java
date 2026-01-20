package model.dao;

import model.dto.ProductionDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductionDAOTest {

    private static ProductionDAO productionDAO;
    private static JdbcDataSource dataSource;

    // Dummy IDs for Foreign Keys
    private static final int DUMMY_USER_PM = 10;
    private static final int DUMMY_USER_CD = 20;
    private static int savedPmID = 0;
    private static int savedCdID = 0;
    private static int savedProductionID = 0;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb_prod;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        productionDAO = new ProductionDAO(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE User (" +
                    "UserID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "FirstName VARCHAR(50) NOT NULL, " +
                    "LastName VARCHAR(50) NOT NULL, " +
                    "PasswordHash VARCHAR(255) NOT NULL, " +
                    "PhoneNumber CHAR(10) NOT NULL, " +
                    "Role ENUM('Performer', 'CastingDirector', 'ProductionManager') NOT NULL, " +
                    "Email VARCHAR(100) UNIQUE NOT NULL, " +
                    "RegistrationDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            stmt.execute("CREATE TABLE Production_Manager (" +
                    "PmID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "UserID INT, " +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE" +
                    ");");

            stmt.execute("CREATE TABLE Casting_Director (" +
                    "CdID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "UserID INT, " +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE" +
                    ");");

            stmt.execute("CREATE TABLE Production (" +
                    "ProductionID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "Title VARCHAR(255) NOT NULL, " +
                    "Type ENUM('Serie TV', 'Film', 'Teatro', 'Musical', 'Pubblicità', 'Documentario', 'Cortometraggio', 'Web Series', 'Altro') NOT NULL, " +
                    "CreationDate DATETIME NOT NULL, " +
                    "PmID INT NOT NULL, " +
                    "FOREIGN KEY (PmID) REFERENCES Production_Manager(PmID) ON DELETE CASCADE" +
                    ");");

            stmt.execute("CREATE TABLE Team (" +
                    "ProductionID INT, " +
                    "CdID INT, " +
                    "PRIMARY KEY(ProductionID, CdID), " +
                    "FOREIGN KEY (ProductionID) REFERENCES Production(ProductionID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (CdID) REFERENCES Casting_Director(CdID) ON DELETE CASCADE" +
                    ");");


            stmt.execute("INSERT INTO User (UserID, FirstName, LastName, PasswordHash, PhoneNumber, Role, Email) " +
                    "VALUES (" + DUMMY_USER_PM + ", 'Prod', 'Manager', 'hash', '1234567890', 'ProductionManager', 'pm@test.com')");

            stmt.execute("INSERT INTO Production_Manager (UserID) VALUES (" + DUMMY_USER_PM + ")");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (rs.next()) savedPmID = rs.getInt(1);
            }

            stmt.execute("INSERT INTO User (UserID, FirstName, LastName, PasswordHash, PhoneNumber, Role, Email) " +
                    "VALUES (" + DUMMY_USER_CD + ", 'Cast', 'Director', 'hash', '0987654321', 'CastingDirector', 'cd@test.com')");

            stmt.execute("INSERT INTO Casting_Director (UserID) VALUES (" + DUMMY_USER_CD + ")");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (rs.next()) savedCdID = rs.getInt(1);
            }
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE Team");
            stmt.execute("DROP TABLE Production");
            stmt.execute("DROP TABLE Casting_Director");
            stmt.execute("DROP TABLE Production_Manager");
            stmt.execute("DROP TABLE User");
        }
    }

    @Test
    @Order(1)
    void testSaveInsert() throws SQLException {
        ProductionDTO prod = new ProductionDTO();
        prod.setTitle("Breaking Bad");
        prod.setType(ProductionDTO.Type.Serie_TV); // Maps to 'Serie TV'
        prod.setCreationDate(LocalDateTime.now());
        prod.setPmID(savedPmID);

        productionDAO.save(prod);

        assertTrue(prod.getProductionID() > 0, "ProductionID should be generated");
        savedProductionID = prod.getProductionID();

        ProductionDTO retrieved = productionDAO.getByID(savedProductionID);
        assertNotNull(retrieved);
        assertEquals("Breaking Bad", retrieved.getTitle());
        assertEquals(ProductionDTO.Type.Serie_TV, retrieved.getType(), "Enum mapping should work correctly");
    }

    @Test
    @Order(2)
    void testGetByID() throws SQLException {
        ProductionDTO prod = productionDAO.getByID(savedProductionID);
        assertNotNull(prod);
        assertEquals(savedPmID, prod.getPmID());

        assertNull(productionDAO.getByID(9999));
    }

    @Test
    @Order(3)
    void testSaveUpdate() throws SQLException {
        ProductionDTO prod = productionDAO.getByID(savedProductionID);
        assertNotNull(prod);

        prod.setTitle("Better Call Saul");
        prod.setType(ProductionDTO.Type.Web_Series); // Maps to 'Web Series'

        productionDAO.save(prod);

        ProductionDTO updated = productionDAO.getByID(savedProductionID);
        assertEquals("Better Call Saul", updated.getTitle());
        assertEquals(ProductionDTO.Type.Web_Series, updated.getType());
    }

    @Test
    @Order(4)
    void testGetByPmID() throws SQLException {
        ProductionDTO prod2 = new ProductionDTO();
        prod2.setTitle("Second Show");
        prod2.setType(ProductionDTO.Type.Film);
        prod2.setCreationDate(LocalDateTime.now());
        prod2.setPmID(savedPmID);
        productionDAO.save(prod2);

        Collection<ProductionDTO> list = productionDAO.getByPmID(savedPmID);

        assertNotNull(list);
        assertTrue(list.size() >= 2, "Should find at least 2 productions for this PM");

        assertTrue(productionDAO.getByPmID(999).isEmpty());
    }

    @Test
    @Order(5)
    void testGetProductionsByCdID() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("INSERT INTO Team (ProductionID, CdID) VALUES (" + savedProductionID + ", " + savedCdID + ")");
        }

        List<ProductionDTO> list = productionDAO.getProductionsByCdID(savedCdID);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(savedProductionID, list.get(0).getProductionID());
        assertEquals("Better Call Saul", list.get(0).getTitle());
    }

    @Test
    @Order(6)
    void testGetTitleByID() throws SQLException {
        String title = productionDAO.getTitleByID(savedProductionID);
        assertEquals("Better Call Saul", title);

        String unknown = productionDAO.getTitleByID(9999);
        assertEquals("Unknown", unknown);
    }

    @Test
    @Order(7)
    void testDelete() throws SQLException {
        boolean deleted = productionDAO.delete(savedProductionID);
        assertTrue(deleted, "Delete should return true");

        assertNull(productionDAO.getByID(savedProductionID), "Production should be gone");

        try (Connection con = dataSource.getConnection();
             var rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM Team WHERE ProductionID=" + savedProductionID)) {
            if(rs.next()) {
                assertEquals(0, rs.getInt(1), "Cascade delete should remove Team entries");
            }
        }
    }
}