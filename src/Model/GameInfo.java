package Model;

/// Object that holds the information of all saved games.
public class GameInfo {

    /// ID of the saved game.
    private int id;

    /// Name of the saved game.
    private String name;

    /// Path to the image that represents the game.
    /// The image name consists of the ID and
    /// a substring of the game's name.
    private String photoPath;

    /// Description of the game (number of players,
    /// how to play the game, etc.).
    private String description;

    public GameInfo(int id, String name, String photoPath, String description) {
        this.id = id;
        this.name = name;
        this.photoPath = photoPath;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}