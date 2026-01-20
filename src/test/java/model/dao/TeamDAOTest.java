package model.dao;

import model.dto.TeamDTO;
import model.dto.UserDTO;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamDAOTest {

    private static TeamDAO teamDAO;
    private static JdbcDataSource dataSource;

    // Static IDs to track generated entities
    private static int prodID_1 = 0;
    private static int prodID_2 = 0;
    private static int cdID_1 = 0;
    private static int cdID_2 = 0;

    @BeforeAll
    static void init() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb_team;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=USER");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        teamDAO = new TeamDAO(dataSource);

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE User (" +
                    "UserID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "FirstName VARCHAR(50), LastName VARCHAR(50), " +
                    "Email VARCHAR(100) UNIQUE)");

            stmt.execute("CREATE TABLE Casting_Director (" +
                    "CdID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "UserID INT, " +
                    "FOREIGN KEY (UserID) REFERENCES User(UserID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE Production (" +
                    "ProductionID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "Title VARCHAR(255))");

            stmt.execute("CREATE TABLE Team (" +
                    "ProductionID INT, " +
                    "CdID INT, " +
                    "PRIMARY KEY(ProductionID, CdID), " +
                    "FOREIGN KEY (ProductionID) REFERENCES Production(ProductionID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (CdID) REFERENCES Casting_Director(CdID) ON DELETE CASCADE" +
                    ");");

            stmt.execute("INSERT INTO User (FirstName, LastName, Email) VALUES ('Carlo', 'Rossi', 'carlo@test.com')");
            stmt.execute("INSERT INTO User (FirstName, LastName, Email) VALUES ('Marta', 'Bianchi', 'marta@test.com')");

            stmt.execute("INSERT INTO Casting_Director (UserID) VALUES (1)");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) cdID_1 = rs.getInt(1); }

            stmt.execute("INSERT INTO Casting_Director (UserID) VALUES (2)");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) cdID_2 = rs.getInt(1); }

            stmt.execute("INSERT INTO Production (Title) VALUES ('Breaking Bad')");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) prodID_1 = rs.getInt(1); }

            stmt.execute("INSERT INTO Production (Title) VALUES ('Stranger Things')");
            try (var rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) { if (rs.next()) prodID_2 = rs.getInt(1); }
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("DROP TABLE Team");
            stmt.execute("DROP TABLE Production");
            stmt.execute("DROP TABLE Casting_Director");
            stmt.execute("DROP TABLE User");
        }
    }

    @Test
    @Order(1)
    void testSave() throws SQLException {
        TeamDTO team = new TeamDTO();
        team.setProductionID(prodID_1);
        team.setCdID(cdID_1);

        teamDAO.save(team);

        assertTrue(teamDAO.exists(prodID_1, cdID_1), "The association should exist in DB after save");
    }


    @Test
    @Order(2)
    void testGetTeamMembers() throws SQLException {
        List<UserDTO> members = teamDAO.getTeamMembers(prodID_1);

        assertNotNull(members);
        assertEquals(1, members.size());

        UserDTO member = members.get(0);
        assertEquals("Carlo", member.getFirstName());
        assertEquals("Rossi", member.getLastName());
        assertEquals("carlo@test.com", member.getEmail());
    }

    @Test
    @Order(3)
    void testGetAvailableCastingDirectors() throws SQLException {
        // Scenario:
        // CD_1 is in Prod_1.
        // CD_2 is NOT in Prod_1.

        List<UserDTO> available = teamDAO.getAvailableCastingDirectors(prodID_1);

        assertNotNull(available);
        assertEquals(1, available.size(), "Should find 1 available CD");
        assertEquals("Marta", available.get(0).getFirstName(), "Marta (CD_2) should be available");

        // Verify exclusion: Carlo (CD_1) should NOT be in this list
        boolean containsCarlo = available.stream().anyMatch(u -> u.getEmail().equals("carlo@test.com"));
        assertFalse(containsCarlo, "Member already in team should not be listed as available");
    }

    @Test
    @Order(4)
    void testGetByCdID() throws SQLException {
        List<TeamDTO> assignments = teamDAO.getByCdID(cdID_1);

        assertFalse(assignments.isEmpty());
        assertEquals(prodID_1, assignments.get(0).getProductionID());

        assertTrue(teamDAO.getByCdID(cdID_2).isEmpty());
    }

    @Test
    @Order(5)
    void testGetByProductionID() throws SQLException {
        List<TeamDTO> members = teamDAO.getByProductionID(prodID_1);

        assertNotNull(members, "The returned list should never be null");
        assertFalse(members.isEmpty(), "Production 1 should have members assigned");

        assertEquals(prodID_1, members.get(0).getProductionID(), "The ProductionID of the result must match the query");

        List<TeamDTO> emptyList = teamDAO.getByProductionID(99999);

        assertNotNull(emptyList, "Should return an empty list, not null, for invalid IDs");
        assertTrue(emptyList.isEmpty(), "List should be empty for a production with no team");
    }

    @Test
    @Order(6)
    void testExists() throws SQLException {
        TeamDTO newLink = new TeamDTO();
        newLink.setProductionID(prodID_2);
        newLink.setCdID(cdID_2);
        teamDAO.save(newLink);

        boolean doesExist = teamDAO.exists(prodID_2, cdID_2);
        assertTrue(doesExist, "Exists must return TRUE for a pair saved in the DB");

        boolean wrongProd = teamDAO.exists(99999, cdID_2);
        assertFalse(wrongProd, "Exists must return FALSE if Production ID does not exist");

        boolean wrongCd = teamDAO.exists(prodID_2, 99999);
        assertFalse(wrongCd, "Exists must return FALSE if Casting Director ID does not exist");

        boolean notLinked = teamDAO.exists(prodID_1, cdID_2);
        assertFalse(notLinked, "Exists must return FALSE for valid IDs that are NOT linked together");
    }

    @Test
    @Order(7)
    void testDelete() throws SQLException {
        boolean deleted = teamDAO.delete(prodID_1, cdID_1);

        assertTrue(deleted, "Delete should return true when removing an existing link");
        assertFalse(teamDAO.exists(prodID_1, cdID_1), "Association should be gone after delete");

        assertTrue(teamDAO.getByProductionID(prodID_1).isEmpty());

        assertFalse(teamDAO.delete(prodID_1, cdID_1), "Delete should return false if link does not exist");
    }
}