package Utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

/// Helper class for file manipulation.
public class FileHelper {

    /// The default path to the 'images' folder.
    public static String imageFolderPath = "C:/ProgramData/BGMenu/images/";

    /// Gets a path to an image chosen by the user.
    public static String getImagePath(Component parent) throws IOException {

        BGLogger.info("Creating/Checking images directory");
        Path imagesPath = Paths.get(imageFolderPath);
        Files.createDirectories(imagesPath);

        BGLogger.info("Starting FileChooser");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File
                (System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "Pictures"));

        // Only allow image files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            BGLogger.info("FileChooser: image chosen");

            File chosenFile = fileChooser.getSelectedFile();

            String chosenPath = chosenFile.getAbsolutePath();
            BGLogger.info("Chosen image path: " + chosenPath);

            return chosenPath;
        }

        BGLogger.info("FileChooser: image not chosen");

        return "";
    }

    /// Copies an image from the user's computer to the 'images' folder.
    public static String  copyToImageFolder(String gameName, String path) {
        BGLogger.info("Starting copy for [" +gameName + "] to images folder." );
        BGLogger.info("Current image path: " + path);

        Path sourcePath = Paths.get(path);

        String fileName = sourcePath.getFileName().toString();
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
        }

        String name = gameName.trim();
        int endIndex = Math.min(name.length(), 9);
        String baseName = name.substring(0, endIndex);

        Path targetPath = Paths.get(imageFolderPath + baseName + extension);
        int counter = 1;

        while (Files.exists(targetPath)) {
            targetPath = Paths.get(imageFolderPath + baseName + counter + extension);
            counter++;
        }

        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            String targetString = targetPath.toString();
            BGLogger.info("Image copied to: " + targetString);

            return targetString;
        } catch (IOException e) {
            BGLogger.error("Error copying image from " + path + " for game [" +gameName + "]" );
            return null;
        }
    }
}