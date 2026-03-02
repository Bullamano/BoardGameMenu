package DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Model.GameInfo;
import Utils.BGLogger;

public class DatabaseHelper {

    /// Creates the application's table if it does not exist.
    public static void createAppTable() {
        String sql = "CREATE TABLE IF NOT EXISTS games ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT NOT NULL,"
                + " photoPath TEXT NOT NULL,"
                + " description TEXT NOT NULL);";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
            BGLogger.info("DB: Created/Verified database!");
        } catch (SQLException ex) {
            BGLogger.error("DB: Error creating table - Exception: " + ex);
        }
    }

    /// Gets all saved games from the DB.
    /// @return A list of GameInfo objects containing all the previously
    /// added games. This list is what populates the lstAddedGames.
    public static List<GameInfo> getAllGames() {

        BGLogger.info("DB: Getting all games...");

        List<GameInfo> games = new ArrayList<>();
        String sql = "SELECT id, name, photoPath, description FROM games ORDER BY name ASC";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    games.add(new GameInfo(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("photoPath"),
                            rs.getString("description")
                    ));
                }

            }

            BGLogger.info("DB: Got all games!");
        } catch (SQLException ex) {
            BGLogger.error("DB: Error getting all games - Exception: " + ex);

            //TODO show error message
            throw new RuntimeException(ex);
        }

        return games;
    }

    /// Adds a game to the DB.
    /// @param name The name of the game that will be saved.
    /// @param path The path to the image that will be copied to
    /// the ProgramData folder and be associated with the game.
    /// @param description The description of the game.
    /// @return Returns a GameInfo object.
    public static GameInfo addGame(String name, String path, String description) throws Exception {

        BGLogger.info("DB: adding game (" + name + " | " + description + " | " + path + ")");

        String sql = "INSERT INTO games(name, photoPath, description) VALUES(?, ?, ?)";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, name);
                pstmt.setString(2, path);
                pstmt.setString(3, description);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    BGLogger.info("DB: added game " + name + "to database!");

                    int id = rs.getInt(1);
                    return new GameInfo(id, name, path, description);
                }
            }
        } catch (SQLException ex) {
            BGLogger.error("DB: Error adding to database - Exception: " + ex);

            throw new RuntimeException(ex);
        }

        return null;
    }

    /// Gets a game from the DB given its ID.
    /// @return The GameInfo with the given ID.
    public static GameInfo getGameById(int id) {

        BGLogger.info("DB: searching by game with ID: " + id);

        String sql = "SELECT * FROM games WHERE id = ?";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {

                    BGLogger.info("DB: found game with ID: " + id);

                    return new GameInfo(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("photoPath"),
                            rs.getString("description")
                    );
                }

            }
        } catch (SQLException ex) {
            BGLogger.error("DB: Error searching database - Exception: " + ex);
            throw new RuntimeException(ex);
        }

        return null; // Not found
    }

    /// Gets a game from the DB given its name.
    /// @return The GameInfo with the associated name.
    public static GameInfo getGameByName(String name) {

        if (name == null || name.isEmpty()) {
            return null;
        }

        BGLogger.info("DB: searching by game with name: " + name);

        String sql = "SELECT * FROM games WHERE name = ?";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {

                    BGLogger.info("DB: found game with name: " + name);

                    return new GameInfo(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("photoPath"),
                            rs.getString("description")
                    );
                }

            }
        } catch (SQLException ex) {
            BGLogger.error("DB: Error searching database - Exception: " + ex);
            throw new RuntimeException(ex);
        }

        return null; // Not found
    }

    /// Updates a previously added game.
    /// @param id The ID of the GameInfo.
    /// @param name The name of the GameInfo
    /// @param path The path of the image that will
    /// overwrite the current image associated with the game.
    /// @param description The description of the GameInfo.
    /// @return The updated GameInfo.
    public static GameInfo updateGame(int id, String name, String path, String description) {

        if (name.isEmpty() || path.isEmpty() || description.isEmpty()) {
            return null;
        }

        BGLogger.info("DB: updating game (" + id + " | " + name + " | " + description + " | " + path + ")");

        String sql = "UPDATE games SET name = ?, photoPath = ?, description = ? WHERE id = ?";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, name);
                pstmt.setString(2, path);
                pstmt.setString(3, description);
                pstmt.setInt(4, id);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    return null;
                }
            }
        } catch (SQLException ex) {
            BGLogger.error("DB: Error updating in database - Exception: " + ex);
            throw new RuntimeException(ex);
        }

        BGLogger.info("DB: updated game " + name + "to database!");

        return getGameById(id);
    }

    /// Deletes a game from the DB given its ID.
    /// @param id The ID of the previously added game.
    public static void deleteGame(int id) {

        BGLogger.info("DB: deleting game with ID: " + id);

        String sql = "DELETE FROM games WHERE id = ?";

        try (Connection conn = Database.connect()) {
            assert conn != null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected == 0) {
                    return;
                }
            }
        } catch (SQLException ex) {
            BGLogger.error("DB: Error deleting from database - Exception: " + ex);
            throw new RuntimeException(ex);
        }

        BGLogger.info("DB: deleted game with ID: " + id);
    }
}

