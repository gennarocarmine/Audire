package model.dao;

import model.dto.ApplicationDTO;
import model.dto.ProductionDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object (DAO) for managing ApplicationDTO entities.
 * <p>
 * This class handles the lifecycle of an application (candidatura).
 * It links a Performer to a Casting and tracks the status of the application
 * </p>
 *
 */
public class ApplicationDAO implements GenericDAO<ApplicationDTO, Integer> {

    private static final List<String> ALLOWED_ORDER_COLUMNS = Arrays.asList(
            "ApplicationID", "SendingDate", "Status", "PerformerID", "CastingID"
    );

    private static final String DEFAULT_ORDER_COLUMN = "SendingDate DESC";

    private final DataSource dataSource;

    public ApplicationDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
    }

    /**
     * Persists an Application to the database.
     * <p>
     * If {@code applicationID} is 0, performs an INSERT.
     * If {@code applicationID} > 0, performs an UPDATE.
     * </p>
     *
     * @param app the application DTO to save.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public void save(ApplicationDTO app) throws SQLException {
        if (app == null || app.getStatus() == null || app.getSendingDate() == null) {
            throw new IllegalArgumentException("ApplicationDTO cannot be null");
        }
        if (app.getPerformerID() <= 0 || app.getCastingID() <= 0) {
            throw new IllegalArgumentException("Application must link to valid Performer and Casting IDs.");
        }

        String sql;
        if (app.getApplicationID() == 0) {
            // INSERT
            sql = "INSERT INTO Application (SendingDate, Status, Feedback, PerformerID, CastingID) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                setStatementParameters(ps, app);

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating application failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        app.setApplicationID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating application failed, no ID obtained.");
                    }
                }
            }
        } else {
            // UPDATE
            sql = "UPDATE Application SET SendingDate=?, Status=?, Feedback=?, PerformerID=?, CastingID=? WHERE ApplicationID=?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                setStatementParameters(ps, app);
                ps.setInt(6, app.getApplicationID());

                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes an application by its ID.
     *
     * @param applicationID the ID of the application to delete.
     * @return true if deleted, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public boolean delete(Integer applicationID) throws SQLException {
        if (applicationID == null || applicationID <= 0) {
            throw new IllegalArgumentException("Invalid ApplicationID");
        }

        String sql = "DELETE FROM Application WHERE ApplicationID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves an application by its unique ID.
     *
     * @param applicationID the ID to search for.
     * @return the ApplicationDTO or null if not found.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public ApplicationDTO getByID(Integer applicationID) throws SQLException {
        if (applicationID == null || applicationID <= 0) return null;

        String sql = "SELECT * FROM Application WHERE ApplicationID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractAppFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all applications submitted by a specific Performer.
     *
     * @param performerID the ID of the performer.
     * @return list of applications.
     * @throws SQLException if database error occurs.
     */
    public Collection<ApplicationDTO> getByPerformerID(Integer performerID) throws SQLException {
        if (performerID == null || performerID <= 0) return new ArrayList<>();

        String sql = "SELECT * FROM Application WHERE PerformerID = ? ORDER BY SendingDate DESC";
        Collection<ApplicationDTO> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, performerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all applications received for a specific Casting call.
     *
     * @param castingID the ID of the casting.
     * @return list of applications.
     * @throws SQLException if database error occurs.
     */
    public Collection<ApplicationDTO> getByCastingID(Integer castingID) throws SQLException {
        if (castingID == null || castingID <= 0) return new ArrayList<>();

        String sql = "SELECT * FROM Application WHERE CastingID = ? ORDER BY SendingDate DESC";
        Collection<ApplicationDTO> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, castingID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractAppFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all applications in the system.
     *
     * @param order the column to sort by.
     * @return a collection of applications.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public Collection<ApplicationDTO> getAll(String order) throws SQLException {
        String actualOrder = (order != null && ALLOWED_ORDER_COLUMNS.contains(order)) ? order : DEFAULT_ORDER_COLUMN;
        String sql = "SELECT * FROM Application ORDER BY " + actualOrder;

        Collection<ApplicationDTO> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractAppFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Checks if a specific performer has already submitted an application for a given casting.
     * <p>
     * This method is typically used to prevent duplicate applications by verifying
     * if an entry already exists in the {@code Application} table linking the specified
     * performer and casting IDs.
     * </p>
     *
     * @param performerID the unique identifier of the Performer.
     * @param castingID   the unique identifier of the Casting.
     * @return {@code true} if an application already exists, {@code false} otherwise.
     * @throws SQLException if a database access error occurs during the query execution.
     */
    public boolean hasApplied(int performerID, int castingID) throws SQLException {
        String sql = "SELECT 1 FROM Application WHERE PerformerID = ? AND CastingID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, performerID);
            ps.setInt(2, castingID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // True se trova una riga
            }
        }
    }

    // --- Helper Methods ---

    private void setStatementParameters(PreparedStatement ps, ApplicationDTO app) throws SQLException {
        if (app.getSendingDate() != null) {
            ps.setTimestamp(1, Timestamp.valueOf(app.getSendingDate()));
        } else {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        }

        if (app.getStatus() != null) {
            ps.setString(2, mapStatusToDb(app.getStatus()));
        } else {
            ps.setString(2, "In attesa"); // Default DB value
        }

        ps.setString(3, app.getFeedback());
        ps.setInt(4, app.getPerformerID());
        ps.setInt(5, app.getCastingID());
    }

    private ApplicationDTO extractAppFromResultSet(ResultSet rs) throws SQLException {
        ApplicationDTO app = new ApplicationDTO();
        app.setApplicationID(rs.getInt("ApplicationID"));

        Timestamp ts = rs.getTimestamp("SendingDate");
        if (ts != null) {
            app.setSendingDate(ts.toLocalDateTime());
        }

        String statusStr = rs.getString("Status");
        if (statusStr != null) {
            app.setStatus(mapDbToStatus(statusStr));
        }

        app.setFeedback(rs.getString("Feedback"));
        app.setPerformerID(rs.getInt("PerformerID"));
        app.setCastingID(rs.getInt("CastingID"));
        return app;
    }

    private String mapStatusToDb(ApplicationDTO.Status status) {
        switch (status) {
            case In_attesa: return "In attesa";
            case Shortlist: return "Shortlist";
            case Selezionata: return "Selezionata";
            case Rifiutata: return "Rifiutata";
            default: return "In attesa";
        }
    }

    private ApplicationDTO.Status mapDbToStatus(String dbValue) {
        try {
            String normalized = dbValue.replace(" ", "_");
            return ApplicationDTO.Status.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ApplicationDTO.Status.In_attesa;
        }
    }
}