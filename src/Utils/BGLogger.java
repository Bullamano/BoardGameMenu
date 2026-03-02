package Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/// The logger for the application.
public class BGLogger {

    private static Logger logger;
    private static final String LOG_DIR = System.getenv("ProgramData") + "\\BGMenu\\log";

    private BGLogger() { }

    /// Initializes the logger
    public static void init() {
        if (logger != null) return; // already initialized

        try {
            // Create/Check log directory
            Path logPath = Paths.get(LOG_DIR);
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }

            // Log file name
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fileName = LOG_DIR + "\\" + date + "_BGLog.txt";

            logger = Logger.getLogger("BGLogger");
            logger.setUseParentHandlers(false); // prevent duplicate logs

            // File handler (append)
            FileHandler fileHandler = new FileHandler(fileName, true);
            fileHandler.setFormatter(new SingleLineFormatter());
            logger.addHandler(fileHandler);

            // Console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SingleLineFormatter());
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Failed to initialize BGLogger: " + e.getMessage());
        }
    }

    /// Info log
    public static void info(String msg) {
        ensureInit();
        logger.info(msg);
    }

    /// Warning log
    public static void warning(String msg) {
        ensureInit();
        logger.warning(msg);
    }

    /// Error log
    public static void error(String msg) {
        ensureInit();
        logger.severe(msg);
    }

    /// Generic log with level
    public static void log(Level level, String msg) {
        ensureInit();
        logger.log(level, msg);
    }

    /// Ensure logger is initialized
    private static void ensureInit() {
        if (logger == null) {
            throw new IllegalStateException("BGLogger is not initialized. Call BGLogger.init() first.");
        }
    }
}

/// Formatter so that the logs are one line per logging instance.
class SingleLineFormatter extends Formatter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String timestamp = sdf.format(new Date(record.getMillis()));
        String level = record.getLevel().getName();
        String message = formatMessage(record); // handles parameterized messages
        return String.format("%s [%s] %s%n", timestamp, level, message);
    }
}