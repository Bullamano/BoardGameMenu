import DB.DatabaseHelper;
import Model.GameInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import UI.RoundButton;
import UI.UIUtils;
import Utils.BGLogger;
import Utils.FileHelper;
import Utils.PDFGenerator;
import com.formdev.flatlaf.FlatDarkLaf;

//TODO verify errors -> try/catch + returns
//TODO add logging
//TODO document methods

//TODO create README to accompany the .exe
//TODO create changelog.txt to accompany the .exe

/// Main screen of the application.
public class Main extends JFrame {

    //region Private Fields
    private JTextField txtGameName;
    private JPanel MainPanel;
    private JTextField txtGameDescription;
    private JTextField txtGamePhotoPath;
    private JButton btnChooseImage;
    private JButton btnAddGame;
    private JLabel lblBGAppTitle;
    private JLabel lblGameName;
    private JLabel lblGamePicture;
    private JLabel lblGameDescription;
    private JButton btnGenerateFile;
    private JList<GameInfo> lstAddedGames;
    private DefaultListModel<GameInfo> listModel;
    private JButton btnDeleteGame;
    private JButton btnThanks;
    //endregion Private Fields

    /// Constructor for the main screen.
    public Main() {

        BGLogger.info("Starting Main screen");

        //region Screen configurations
        setContentPane(MainPanel);
        setTitle("Board game menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        //Games list
        listModel = new DefaultListModel<>();
        lstAddedGames.setModel(listModel);

        BGLogger.info("Starting DB");

        //Populating games list
        DatabaseHelper.createAppTable();
        loadGamesFromDatabase();

        //Idea: Change buttons of the screen in future version?
        //btnAddGame.setForeground(Color.WHITE);
        //btnAddGame.setBackground(new Color(0x242424));
        //btnAddGame.setOpaque(true);

        try {
            Image icon = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/Images/BG_Logo.png")));
            setIconImage(icon);
        } catch (IOException | NullPointerException ex) {
            BGLogger.error("Error while setting application's logo - Exception: " + ex);
        }

        setVisible(true);
        //endregion Screen configurations

        //region Buttons
        btnAddGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Prevents double clicks
                btnAddGame.setEnabled(false);

                String gameName = txtGameName.getText();
                String photoPath = txtGamePhotoPath.getText();
                String gameDescription = txtGameDescription.getText();

                if (gameName.isBlank() || photoPath.isBlank() || gameDescription.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Not all fields are filled. Fill the game name, description and choose an image before adding it to the list of games!");
                    return;
                }

                DatabaseHelper.createAppTable();

                SwingWorker<Void, Void> worker = new SwingWorker<>() {

                    GameInfo game;
                    boolean gameAdded = false;

                    @Override
                    protected Void doInBackground() throws Exception {

                        String savedPath = FileHelper.copyToImageFolder(gameName, photoPath);

                        if (savedPath ==  null || savedPath.isBlank()) {
                            JOptionPane.showMessageDialog(null, "Couldn't copy the desired image to the images folder. Verify the desired image and try again.");
                            return null;
                        }

                        try {
                            game = DatabaseHelper.addGame(gameName, savedPath, gameDescription);
                            gameAdded = true;
                        } catch (Exception ex) {
                            BGLogger.error("Error adding game - Exception: " + ex);
                            JOptionPane.showMessageDialog(null, "There was an error adding the game. Try again later.");
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            //Check for exceptions
                            get();

                            if (!gameAdded) {
                                return;
                            }

                            JOptionPane.showMessageDialog(null, "Game added successfully!");

                            txtGameName.setText("");
                            txtGamePhotoPath.setText("");
                            txtGameDescription.setText("");

                            if (game != null) {
                                listModel.addElement(game);
                            }

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Error: " + ex.getMessage(),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            btnAddGame.setEnabled(true);
                        }
                    }
                };

                worker.execute();
            }
        });

        btnGenerateFile.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF");

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {

                File fileToSave = fileChooser.getSelectedFile();

                // Ensure .pdf extension
                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                }

                btnGenerateFile.setEnabled(false);

                File finalFileToSave = fileToSave;

                SwingWorker<Void, Void> worker = new SwingWorker<>() {

                    boolean wasGenerated = false;

                    @Override
                    protected Void doInBackground() throws Exception {

                        try {
                            List<GameInfo> list = Collections.list(listModel.elements());

                            PDFGenerator.generatePdfFile(finalFileToSave, list);

                            wasGenerated = true;
                        } catch (Exception ex) {
                            BGLogger.error("Error generating PDF - Exception: " + ex);
                            JOptionPane.showMessageDialog(null, "There was an error generating the PDF. Try again later.");
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        btnGenerateFile.setEnabled(true);

                        if (!wasGenerated) {
                            return;
                        }

                        try {
                            get();
                            JOptionPane.showMessageDialog(null, "PDF generated successfully!");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Error generating PDF: " + ex.getMessage(),
                                    "PDF Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

                worker.execute();
            }
        });

        btnDeleteGame.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                GameInfo game = lstAddedGames.getSelectedValue();

                // Prevents double clicks
                btnDeleteGame.setEnabled(false);

                DatabaseHelper.createAppTable();

                SwingWorker<Void, Void> worker = new SwingWorker<>() {

                    boolean wasGameDeleted = false;

                    @Override
                    protected Void doInBackground() throws Exception {

                        try {
                            DatabaseHelper.deleteGame(game.getId());
                            wasGameDeleted = true;
                        } catch (Exception ex) {
                            BGLogger.error("Error adding game - Exception: " + ex);
                            JOptionPane.showMessageDialog(null, "There was an error adding the game. Try again later.");
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            //Check for exceptions
                            get();

                            if (!wasGameDeleted) {
                                return;
                            }

                            JOptionPane.showMessageDialog(null, "Game successfully deleted!");

                            txtGameName.setText("");
                            txtGamePhotoPath.setText("");
                            txtGameDescription.setText("");

                            if (game != null) {
                                listModel.removeElement(game);
                            }

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Error: " + ex.getMessage(),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            btnDeleteGame.setEnabled(true);
                        }
                    }
                };

                worker.execute();
            }
        });

        btnChooseImage.addActionListener(e -> {
            try {
                txtGamePhotoPath.setText(Utils.FileHelper.getImagePath(MainPanel));
            } catch (IOException ex) {
                BGLogger.error("Error choosing image - Exception: " + ex);
                JOptionPane.showMessageDialog(null, "There was an error with the image chosen. Try again later.");
            }
        });

        btnThanks.addActionListener(e -> {
            String text = "<html>"
                    + "Thanks to all my family and friends who allow me to play such wonderful games in such a delightful company.<br><br>"
                    + "Application icon by <a href='https://www.freepik.com/icon/board-games_1587077'>Freepik</a>"
                    + "</html>";

            JEditorPane editorPane = new JEditorPane("text/html", text);
            editorPane.setEditable(false);
            editorPane.setOpaque(false);
            editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            editorPane.addHyperlinkListener(event -> {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(event.getURL().toURI());
                    } catch (Exception ex) {
                        BGLogger.error("Error navigating to website - Exception: " + ex);
                    }
                }
            });

            JOptionPane.showMessageDialog(null, editorPane, "Thanks", JOptionPane.INFORMATION_MESSAGE);
        });
        //endregion Buttons
    }

    /// Application entrypoint.
    static void main() {

        //region Logger init
        BGLogger.init();
        BGLogger.info("===============");
        BGLogger.info("BGMenu started!");
        BGLogger.info("===============");
        //endregion Logger init

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            BGLogger.error("Failed to initialize application theme - Exception: " + ex);
        }

        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            UIUtils.setHandCursorForAllButtons(main.getContentPane());
        });
    }

    /// Loads all games from the database and adds
    /// them to the list of games in the screen.
    //region Private Methods
    private void loadGamesFromDatabase() {

        BGLogger.info("Loading games from DB...");

        SwingWorker<List<GameInfo>, Void> worker = new SwingWorker<>() {

            boolean wereGamesLoaded = false;

            @Override
            protected List<GameInfo> doInBackground() {

                List<GameInfo> games = null;
                try {
                    games = DatabaseHelper.getAllGames();
                    wereGamesLoaded = true;
                } catch (Exception ex) {
                    BGLogger.error("Error loading games for main screen - Exception: " + ex);
                }

                return games;
            }

            @Override
            protected void done() {
                try {
                    List<GameInfo> games = get();

                    if (!wereGamesLoaded) {
                        return;
                    }

                    //Clear games from lstAddedGames
                    listModel.clear();

                    for (GameInfo game : games) {
                        listModel.addElement(game);
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Error loading games: " + e.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /// Creates non-standard UI components.
    private void createUIComponents() {
        btnThanks = new RoundButton("Click");
        btnThanks.setMinimumSize(new Dimension(30, 30));
        btnThanks.setMaximumSize(new Dimension(30, 30));
        btnThanks.setPreferredSize(new Dimension(30, 30));
    }
    //endregion Private Methods
}