package DB;

import Utils.BGLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class Database {

    /// JDBC SQLITE path to the application database.
    private static final String URL = "jdbc:sqlite:C:/ProgramData/BGMenu/data/bgmenu.db";

    /// Connection to the database.
    public static Connection connect() {
        try {
            File dir = new File("C:/ProgramData/BGMenu/data");
            if (!dir.exists()) {
                BGLogger.info("Creating data folder...");
                boolean createdFolder = dir.mkdirs();
                BGLogger.info("Create data folder result: " + createdFolder);
            }

            return DriverManager.getConnection(URL);

        } catch (SQLException ex) {
            BGLogger.error("Error connecting to DB - Exception: " + ex);
            return null;
        }
    }
}