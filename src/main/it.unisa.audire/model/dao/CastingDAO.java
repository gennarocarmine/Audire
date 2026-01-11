package model.dao;

import model.dto.CastingDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object (DAO) for managing CastingDTO entities.
 * <p>
 * This class manages the lifecycle of Casting calls (annunci di casting).
 * It links a specific Production to a Casting Director and defines the requirements
 * (Category, Deadline, Description) for Performers.
 * </p>
 *
 */
public class CastingDAO implements GenericDAO<CastingDTO, Integer> {

    private static final List<String> ALLOWED_ORDER_COLUMNS = Arrays.asList(
            "CastingID", "Location", "Title", "PublishDate", "DeadLine", "Category", "ProductionID"
    );

    private static final String DEFAULT_ORDER_COLUMN = "CastingID";

    private final DataSource dataSource;

    public CastingDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
    }

    /**
     * Persists a Casting announcement to the database.
     *
     * <p>
     * If {@code CastingID} is 0, performs an INSERT.
     * If {@code CastingID} > 0, performs an UPDATE.
     * </p>
     *
     * @param casting the CastingDTO to save.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public void save(CastingDTO casting) throws SQLException {
        if (casting == null || casting.getLocation() == null || casting.getLocation().trim().isEmpty() ||
                casting.getCategory() == null || casting.getDescription() == null ||
                casting.getDescription().trim().isEmpty() || casting.getPublishDate() == null ||
                casting.getDeadline() == null || casting.getTitle() == null || casting.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("CastingDTO cannot be null");
        }
        if (casting.getCdID() <= 0 || casting.getProductionID() <= 0) {
            throw new IllegalArgumentException("Casting must be linked to a valid Casting Director and Production.");
        }

        String sql;
        if (casting.getCastingID() == 0) {
            // INSERT
            sql = "INSERT INTO Casting (Location, Category, Description, PublishDate, DeadLine, Title, CdID, ProductionID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                setStatementParameters(ps, casting);

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating casting failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        casting.setCastingID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating casting failed, no ID obtained.");
                    }
                }
            }
        } else {
            // UPDATE
            sql = "UPDATE Casting SET Location=?, Category=?, Description=?, PublishDate=?, DeadLine=?, Title=?, CdID=?, ProductionID=? WHERE CastingID=?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                setStatementParameters(ps, casting);
                ps.setInt(9, casting.getCastingID());

                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes a casting by its ID.
     *
     * @param castingID the ID of the casting to delete.
     * @return true if deleted, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public boolean delete(Integer castingID) throws SQLException {
        if (castingID == null || castingID <= 0) {
            throw new IllegalArgumentException("Invalid CastingID");
        }

        String sql = "DELETE FROM Casting WHERE CastingID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, castingID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a casting by its unique ID.
     *
     * @param castingID the ID to search for.
     * @return the castingDTO or null if not found.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public CastingDTO getByID(Integer castingID) throws SQLException {
        if (castingID == null || castingID <= 0) return null;

        String sql = "SELECT * FROM Casting WHERE CastingID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, castingID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCastingFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all castings associated with a specific Production.
     *
     * @param productionID the ID of the production.
     * @return a collection of castings.
     * @throws SQLException if a database error occurs.
     */
    public Collection<CastingDTO> getByProductionID(Integer productionID) throws SQLException {
        if (productionID == null || productionID <= 0) return new ArrayList<>();

        String sql = "SELECT * FROM Casting WHERE ProductionID = ? ORDER BY PublishDate DESC";
        Collection<CastingDTO> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productionID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractCastingFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all castings managed by a specific Casting Director.
     *
     * @param cdID the ID of the casting director.
     * @return a collection of castings.
     * @throws SQLException if a database error occurs.
     */
    public Collection<CastingDTO> getByCdID(Integer cdID) throws SQLException {
        if (cdID == null || cdID <= 0) return new ArrayList<>();

        String sql = "SELECT * FROM Casting WHERE CdID = ? ORDER BY PublishDate DESC";
        Collection<CastingDTO> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cdID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractCastingFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all castings in the system.
     *
     * @param order the column to sort by.
     * @return a collection of castings.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public Collection<CastingDTO> getAll(String order) throws SQLException {
        String actualOrder = (order != null && ALLOWED_ORDER_COLUMNS.contains(order)) ? order : DEFAULT_ORDER_COLUMN;
        String sql = "SELECT * FROM Casting ORDER BY " + actualOrder;

        Collection<CastingDTO> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractCastingFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves all active casting calls (where the deadline is today or in the future) for the Home Page.
     *
     * @return a {@code List} of active {@link CastingDTO} objects, ordered by publish date descending.
     * @throws SQLException if a database access error occurs.
     */
    public List<CastingDTO> getAllActive() throws SQLException {
        String sql = "SELECT c.*, p.Title as ProductionTitle " +
                "FROM Casting c " +
                "JOIN Production p ON c.ProductionID = p.ProductionID " +
                "WHERE c.DeadLine >= CURRENT_DATE " +
                "ORDER BY c.PublishDate DESC";

        List<CastingDTO> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractCastingFromResultSet(rs));
            }
        }
        return list;
    }

    // --- Helper Methods ---

    private void setStatementParameters(PreparedStatement ps, CastingDTO casting) throws SQLException {
        ps.setString(1, casting.getLocation());

        if (casting.getCategory() != null) {
            ps.setString(2, mapCategoryToDb(casting.getCategory()));
        } else {
            ps.setNull(2, Types.VARCHAR);
        }

        ps.setString(3, casting.getDescription());

        // Handle PublishDate
        if (casting.getPublishDate() != null) {
            ps.setTimestamp(4, Timestamp.valueOf(casting.getPublishDate()));
        } else {
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        }

        // Handle DeadLine
        if (casting.getDeadline() != null) {
            ps.setTimestamp(5, Timestamp.valueOf(casting.getDeadline()));
        } else {
            ps.setNull(5, Types.DATE);
        }

        ps.setString(6, casting.getTitle());
        ps.setInt(7, casting.getCdID());
        ps.setInt(8, casting.getProductionID());
    }

    private CastingDTO extractCastingFromResultSet(ResultSet rs) throws SQLException {
        CastingDTO c = new CastingDTO();
        c.setCastingID(rs.getInt("CastingID"));
        c.setLocation(rs.getString("Location"));

        String catStr = rs.getString("Category");
        if (catStr != null) {
            c.setCategory(mapDbToCategory(catStr));
        }

        c.setDescription(rs.getString("Description"));

        Timestamp ts = rs.getTimestamp("PublishDate");
        if (ts != null) {
            c.setPublishDate(ts.toLocalDateTime());
        }

        Timestamp ts2 = rs.getTimestamp("DeadLine");
        if (ts2 != null) {
            c.setDeadline(ts2.toLocalDateTime());
        }

        c.setTitle(rs.getString("Title"));
        c.setCdID(rs.getInt("CdID"));
        c.setProductionID(rs.getInt("ProductionID"));

        return c;
    }

    // Mapping Logic for Categories (reused from PerformerDAO logic)
    private String mapCategoryToDb(CastingDTO.Category category) {
        switch (category) {
            case Attore_Attrice: return "Attore/Attrice";
            case Doppiatore_trice: return "Doppiatore/trice";
            default: return category.name();
        }
    }

    private CastingDTO.Category mapDbToCategory(String dbValue) {
        switch (dbValue) {
            case "Attore/Attrice": return CastingDTO.Category.Attore_Attrice;
            case "Doppiatore/trice": return CastingDTO.Category.Doppiatore_trice;
            default:
                try {
                    return CastingDTO.Category.valueOf(dbValue);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }
}