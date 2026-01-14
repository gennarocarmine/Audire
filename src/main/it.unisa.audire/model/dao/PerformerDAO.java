package model.dao;

import model.dto.PerformerDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Data Access Object (DAO) for managing Performer entities.
 * <p>
 * This class provides the persistence layer for the "Performer" table in the database.
 * It implements the CRUD (Create, Read, Update, Delete) operations and manages
 * the mapping between the relational database schema and the Java object model.
 * </p>
 *
 */
public class PerformerDAO implements GenericDAO<PerformerDTO, Integer>{

    private static final List<String> ALLOWED_ORDER_COLUMNS = Arrays.asList(
            "PerformerID", "Gender", "Category"
    );

    private static final String DEFAULT_ORDER_COLUMN = "PerformerID";

    private final DataSource dataSource;

    /**
     * Constructs a new PerformerDAO with the specified DataSource.
     *
     * @param dataSource the DataSource used to obtain database connections.
     * @throws NullPointerException if the provided dataSource is null.
     */
    public PerformerDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
    }

    /**
     * Persists a Performer object to the database.
     * <p>
     * This method handles both <b>INSERT</b> and <b>UPDATE</b> operations:
     * <ul>
     * <li>If {@code performerID} is 0, a new record is created (INSERT), and the generated ID is set on the DTO.</li>
     * <li>If {@code performerID} is greater than 0, the existing record is updated (UPDATE).</li>
     * </ul>
     * </p>
     *
     * @param performer the PerformerDTO object to save.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalArgumentException if the performer is null, lacks a valid UserID, or has missing mandatory fields.
     */
    public void save(PerformerDTO performer) throws SQLException {
        if (performer == null || performer.getGender() == null || performer.getCategory() == null ||
                performer.getDescription() == null || performer.getDescription().trim().isEmpty() ||
                performer.getProfilePhoto() == null || performer.getProfilePhoto().trim().isEmpty()) {
            throw new IllegalArgumentException("Performer cannot be null and must have valid Gender, Category, Description, and Photo.");
        }

        if (performer.getUserID() <= 0) {
            throw new IllegalArgumentException("Performer must be associated with a valid UserID");
        }

        String sql;

        if (performer.getPerformerID() == 0) {
            // INSERT
            sql = "INSERT INTO Performer (Gender, Category, Description, CV_Data, CV_MimeType, ProfilePhoto, UserID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                setStatementParameters(ps, performer);

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating performer failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        performer.setPerformerID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating performer failed, no ID obtained.");
                    }
                }
            }
        } else {
            // UPDATE
            sql = "UPDATE Performer SET Gender=?, Category=?, Description=?, CV_Data=?, CV_MimeType=?, ProfilePhoto=?, UserID=? WHERE PerformerID=?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                setStatementParameters(ps, performer);
                ps.setInt(8, performer.getPerformerID());

                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes a performer from the database by their unique ID.
     *
     * @param performerID the unique identifier of the performer to delete.
     * @return {@code true} if the deletion was successful (row was found and removed), {@code false} otherwise.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalArgumentException if {@code performerID} is null or non-positive.
     */
    public boolean delete(Integer performerID) throws SQLException {
        if (performerID == null || performerID <= 0) {
            throw new IllegalArgumentException("Invalid PerformerID");
        }

        String sql = "DELETE FROM Performer WHERE PerformerID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, performerID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a Performer by their unique primary key.
     *
     * @param performerID the ID to search for.
     * @return the PerformerDTO if found, or {@code null} if no record matches the ID.
     * @throws SQLException if a database access error occurs.
     */
    public PerformerDTO getByID(Integer performerID) throws SQLException {
        if (performerID == null || performerID <= 0) return null;

        String sql = "SELECT * FROM Performer WHERE PerformerID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, performerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPerformerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a Performer profile associated with a specific User account.
     * <p>
     * This method is essential for loading the performer's specific data immediately after
     * a generic User login.
     * </p>
     *
     * @param userID the foreign key representing the User account.
     * @return the PerformerDTO if found, or {@code null} if the user has not created a performer profile.
     * @throws SQLException if a database access error occurs.
     */
    public PerformerDTO getByUserID(Integer userID) throws SQLException {
        if (userID == null || userID <= 0) return null;

        String sql = "SELECT * FROM Performer WHERE UserID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractPerformerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all performers from the database, optionally sorted.
     *
     * @param order the column name to sort by. Must be one of: {@code "PerformerID", "Gender", "Category"}.
     * If null or invalid, defaults to "PerformerID".
     * @return a Collection of PerformerDTO objects.
     * @throws SQLException if a database access error occurs.
     */
    public Collection<PerformerDTO> getAll(String order) throws SQLException {
        String actualOrder = (order != null && ALLOWED_ORDER_COLUMNS.contains(order)) ? order : DEFAULT_ORDER_COLUMN;
        String sql = "SELECT * FROM Performer ORDER BY " + actualOrder;

        Collection<PerformerDTO> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractPerformerFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves only the Curriculum Vitae (CV) binary data and its MIME type for a specific performer.
     *
     * @param performerID the unique identifier of the performer.
     * @return a {@link PerformerDTO} populated only with the CV binary data and MIME type,
     * or {@code null} if no performer is found with the given ID.
     * @throws SQLException if a database access error occurs during the query execution.
     */
    public PerformerDTO getCvData(int performerID) throws SQLException {
        String sql = "SELECT CV_Data, CV_MimeType FROM Performer WHERE PerformerID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, performerID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PerformerDTO p = new PerformerDTO();
                    p.setCvData(rs.getBytes("CV_Data"));
                    p.setCvMimeType(rs.getString("CV_MimeType"));
                    return p;
                }
            }
        }
        return null;
    }

    // --- Helper Methods ---

    /**
     * Sets the parameters of the PreparedStatement based on the PerformerDTO.
     * Handles specific mapping for Enums and Nullable fields.
     *
     * @param ps the PreparedStatement to populate.
     * @param performer the DTO containing the data.
     * @throws SQLException if parameter setting fails.
     */
    private void setStatementParameters(PreparedStatement ps, PerformerDTO performer) throws SQLException {
        if (performer.getGender() != null) {
            ps.setString(1, performer.getGender().name());
        } else {
            ps.setNull(1, Types.CHAR);
        }

        // Conversion logic: DTO "Attore_Attrice" -> DB "Attore/Attrice"
        if (performer.getCategory() != null) {
            String dbValue = mapCategoryToDb(performer.getCategory());
            ps.setString(2, dbValue);
        } else {
            ps.setNull(2, Types.VARCHAR);
        }

        ps.setString(3, performer.getDescription());
        ps.setBytes(4, performer.getCvData());
        ps.setString(5, performer.getCvMimeType());
        ps.setString(6, performer.getProfilePhoto());
        ps.setInt(7, performer.getUserID());
    }

    /**
     * Extracts a PerformerDTO from the current row of the ResultSet.
     *
     * @param rs the ResultSet cursor.
     * @return a populated PerformerDTO.
     * @throws SQLException if column access fails.
     */
    private PerformerDTO extractPerformerFromResultSet(ResultSet rs) throws SQLException {
        PerformerDTO p = new PerformerDTO();
        p.setPerformerID(rs.getInt("PerformerID"));

        // Gender Mapping
        String genderStr = rs.getString("Gender");
        if (genderStr != null) {
            try {
                p.setGender(PerformerDTO.Gender.valueOf(genderStr));
            } catch (IllegalArgumentException e) {
                // Log error or ignore if enum constraint is violated in DB
            }
        }

        // Category Mapping (DB "Attore/Attrice" -> DTO "Attore_Attrice")
        String catStr = rs.getString("Category");
        if (catStr != null) {
            p.setCategory(mapDbToCategory(catStr));
        }

        p.setDescription(rs.getString("Description"));
        p.setCvData(rs.getBytes("CV_Data"));
        p.setCvMimeType(rs.getString("CV_MimeType"));
        p.setProfilePhoto(rs.getString("ProfilePhoto"));
        p.setUserID(rs.getInt("UserID"));

        return p;
    }

    /**
     * Maps the Java Enum Category to the Database String format.
     * Handles special characters (like slashes) that cannot be part of Java identifiers.
     *
     * @param category the Java Enum value.
     * @return the corresponding String value for the Database.
     */
    private String mapCategoryToDb(PerformerDTO.Category category) {
        switch (category) {
            case Attore_Attrice: return "Attore/Attrice";
            case Doppiatore_trice: return "Doppiatore/trice";
            default: return category.name();
        }
    }

    /**
     * Maps the Database String format to the Java Enum Category.
     * Reverses the logic of mapCategoryToDb(PerformerDTO.Category).
     *
     * @param dbValue the String value from the Database.
     * @return the corresponding Java Enum value, or null if mapping fails.
     */
    private PerformerDTO.Category mapDbToCategory(String dbValue) {
        switch (dbValue) {
            case "Attore/Attrice": return PerformerDTO.Category.Attore_Attrice;
            case "Doppiatore/trice": return PerformerDTO.Category.Doppiatore_trice;
            default:
                try {
                    return PerformerDTO.Category.valueOf(dbValue);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }
}