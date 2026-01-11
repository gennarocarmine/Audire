package model.dao;

import model.dto.ProductionDTO;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data Access Object (DAO) for managing ProductionDTO entities.
 * <p>
 * This class handles the persistence of Productions.
 * It manages the lifecycle of a production, which is owned by a Production Manager.
 * </p>
 *
 */
public class ProductionDAO implements GenericDAO<ProductionDTO, Integer> {

    private static final List<String> ALLOWED_ORDER_COLUMNS = Arrays.asList(
            "ProductionID", "Title", "Type", "CreationDate", "pmID"
    );

    private static final String DEFAULT_ORDER_COLUMN = "ProductionID";

    private final DataSource dataSource;

    /**
     * Constructs a new {@code ProductionDAO}.
     *
     * @param dataSource the data source for database connections.
     */
    public ProductionDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
    }

    /**
     * Persists a Production to the database.
     * <p>
     * If {@code ProductionID} is 0, performs an INSERT.
     * If {@code ProductionID} > 0, performs an UPDATE.
     * </p>
     *
     * @param production the production DTO to save.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public void save(ProductionDTO production) throws SQLException {
        if (production == null || production.getTitle() == null || production.getTitle().trim().isEmpty() ||
                production.getType() == null || production.getCreationDate() == null ) {
            throw new IllegalArgumentException("ProductionDTO cannot be null");
        }
        if (production.getPmID() <= 0) {
            throw new IllegalArgumentException("A Production must be assigned to a valid Production Manager (PmID).");
        }

        String sql;
        if (production.getProductionID() == 0) {
            // INSERT
            sql = "INSERT INTO Production (Title, Type, CreationDate, PmID) VALUES (?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                setStatementParameters(ps, production);

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating production failed, no rows affected.");
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        production.setProductionID(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Creating production failed, no ID obtained.");
                    }
                }
            }
        } else {
            // UPDATE
            sql = "UPDATE Production SET Title=?, Type=?, CreationDate=?, PmID=? WHERE ProductionID=?";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                setStatementParameters(ps, production);
                ps.setInt(5, production.getProductionID());

                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes a production by its ID.
     * <p>
     * <b>Note:</b> Due to cascade constraints, deleting a production typically deletes
     * all associated Castings and Team assignments.
     * </p>
     *
     * @param productionID the ID of the production to delete.
     * @return true if deleted, false otherwise.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public boolean delete(Integer productionID) throws SQLException {
        if (productionID == null || productionID <= 0) {
            throw new IllegalArgumentException("Invalid ProductionID");
        }

        String sql = "DELETE FROM Production WHERE ProductionID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productionID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a production by its unique ID.
     *
     * @param productionID the ID to search for.
     * @return the ProductionDTO or null if not found.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public ProductionDTO getByID(Integer productionID) throws SQLException {
        if (productionID == null || productionID <= 0) return null;

        String sql = "SELECT * FROM Production WHERE ProductionID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productionID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProductionFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all productions owned by a specific Production Manager.
     *
     * @param pmID the Production Manager ID.
     * @return a collection of productions.
     * @throws SQLException if a database error occurs.
     */
    public Collection<ProductionDTO> getByPmID(Integer pmID) throws SQLException {
        if (pmID == null || pmID <= 0) return new ArrayList<>();

        String sql = "SELECT * FROM Production WHERE PmID = ? ORDER BY CreationDate DESC";
        Collection<ProductionDTO> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, pmID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractProductionFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all productions in the system.
     *
     * @param order the column to sort by.
     * @return a collection of productions.
     * @throws SQLException if a database error occurs.
     */
    @Override
    public Collection<ProductionDTO> getAll(String order) throws SQLException {
        String actualOrder = (order != null && ALLOWED_ORDER_COLUMNS.contains(order)) ? order : DEFAULT_ORDER_COLUMN;
        String sql = "SELECT * FROM Production ORDER BY " + actualOrder;

        Collection<ProductionDTO> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractProductionFromResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves all productions assigned to a specific Casting Director via the Team table.
     * Used to populate the dropdown in the Create Casting form.
     *
     * @param cdID the Casting Director's ID.
     * @return List of ProductionDTOs the CD is working on.
     */
    public List<ProductionDTO> getProductionsByCdID(int cdID) throws SQLException {
        String sql = "SELECT p.* FROM Production p " +
                "JOIN Team t ON p.ProductionID = t.ProductionID " +
                "WHERE t.CdID = ? ORDER BY p.CreationDate DESC";

        List<ProductionDTO> list = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cdID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Reuse your existing extraction method
                    list.add(extractProductionFromResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves only the title of a production given its unique identifier.
     * <p>
     * This is a lightweight utility method designed for scenarios where only the
     * production name is required (e.g., populating tables, lists, or dropdowns),
     * avoiding the overhead of retrieving and mapping the full {@link model.dto.ProductionDTO} object.
     * </p>
     *
     * @param productionID the unique identifier of the production to search for.
     * @return the title of the production as a {@code String} if found; otherwise returns "Unknown".
     * @throws SQLException if a database access error occurs during the query execution.
     */
    public String getTitleByID(int productionID) throws SQLException {
        String sql = "SELECT Title FROM Production WHERE ProductionID = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productionID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Title");
                }
            }
        }
        return "Unknown";
    }

    // --- Helper Methods ---

    private void setStatementParameters(PreparedStatement ps, ProductionDTO prod) throws SQLException {
        ps.setString(1, prod.getTitle());

        // Handle Enum Mapping (Java Enum -> SQL String)
        if (prod.getType() != null) {
            ps.setString(2, mapTypeToDb(prod.getType()));
        } else {
            ps.setNull(2, Types.VARCHAR);
        }

        // Handle Date
        if (prod.getCreationDate() != null) {
            ps.setTimestamp(3, Timestamp.valueOf(prod.getCreationDate()));
        } else {
            // Default to current date if missing, or set null
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        }

        ps.setInt(4, prod.getPmID());
    }

    private ProductionDTO extractProductionFromResultSet(ResultSet rs) throws SQLException {
        ProductionDTO p = new ProductionDTO();
        p.setProductionID(rs.getInt("ProductionID"));
        p.setTitle(rs.getString("Title"));

        String typeStr = rs.getString("Type");
        if (typeStr != null) {
            p.setType(mapDbToType(typeStr));
        }

        Timestamp ts = rs.getTimestamp("CreationDate");
        if (ts != null) {
            p.setCreationDate(ts.toLocalDateTime());
        }

        p.setPmID(rs.getInt("PmID"));
        return p;
    }

    /**
     * Maps Java Enum (e.g., Serie_TV) to DB String (e.g., "Serie TV").
     */
    private String mapTypeToDb(ProductionDTO.Type type) {
        // Assuming Enum values like SERIE_TV, WEB_SERIES needs to become "Serie TV", "Web Series"
        switch (type) {
            case Serie_TV: return "Serie TV";
            case Web_Series: return "Web Series";
            // Map other types normally or adjust as needed
            default: return type.name().charAt(0) + type.name().substring(1).toLowerCase();
        }
    }

    /**
     * Maps DB String (e.g., "Serie TV") to Java Enum (e.g., SERIE_TV).
     */
    private ProductionDTO.Type mapDbToType(String dbValue) {
        try {
            String normalized = dbValue.replace(" ", "_");
            return ProductionDTO.Type.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}