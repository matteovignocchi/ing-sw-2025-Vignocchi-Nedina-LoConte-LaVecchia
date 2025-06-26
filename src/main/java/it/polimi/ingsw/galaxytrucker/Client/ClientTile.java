package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Client-side representation of a ship tile.
 * Contains all the visual and descriptive information needed by the GUI,
 * including tile type, connectors, goods, tokens, rotation, and image.
 * Also includes methods to rotate the tile and load its graphical representation.
 * @author Oleg Nedina
 */
public class ClientTile {
    public String type;
    public int a, b, c, d;
    public int id;
    public boolean idDouble;
    public int max;
    public boolean advance;
    public int capacity;
    public String human;
    public List<String> goods = new ArrayList<>();
    public List<String> tokens = new ArrayList<>();
    public List<Integer> protectedCorners = new ArrayList<>();
    public int rotation;
    @JsonProperty("rotation")

    /**
     * Rotates the tile 90 degrees clockwise.
     * Updates the connector values and the protected corners if present.
     */
    public void rotateRight() {
        int temp = d;
        d = c;
        c = b;
        b = a;
        a = temp;
        rotation++;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(3);
            protectedCorners.set(3, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(0));
            protectedCorners.set(0, tmp);
        }
    }

    /**
     * Rotates the tile 90 degrees counter-clockwise.
     * Updates the connector values and the protected corners if present.
     */
    public void rotateLeft() {
        int temp = a;
        a = b;
        b = c;
        c = d;
        d = temp;
        rotation--;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(0);
            protectedCorners.set(0, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(3));
            protectedCorners.set(3, tmp);
        }
    }


    /**
     * Loads the tile image based on its ID.
     * If the tile ID is 0, a default image is used. If the specific image is not found,
     * a RuntimeException is thrown.
     * @param tileId the ID of the tile
     * @return the Image object associated with the tile
     * @throws RuntimeException if the image is missing or cannot be loaded
     */
    public static Image loadImageById(int tileId) {
        try {
            //TODO ricordarsi che se è non devo caricare nessuna immagine
            if (tileId == 0) {
                tileId = 157;
                String imagePath = "/images/Polytechnic/tiles/GT-new_tiles_16_for web" + tileId + ".png";
                InputStream is = ClientTile.class.getResourceAsStream(imagePath);
                return new Image(is);
            }
            String imagePath = "/images/Polytechnic/tiles/GT-new_tiles_16_for web" + tileId + ".jpg";
            InputStream is = ClientTile.class.getResourceAsStream(imagePath);

            if (is == null) {
                System.err.println("[WARN] Tile image not found: " + imagePath + ", using placeholder.");
                throw new RuntimeException("Tile image not found");
            }

            return new Image(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tile image for ID: " + tileId, e);
        }
    }

    /**
     * Returns the image corresponding to this tile instance.
     * If the image is missing, attempts to load a placeholder image.
     * @return the tile's Image
     * @throws RuntimeException if neither the main image nor the placeholder is available
     */
    public Image getImage() {
        try {
            return loadImageById(this.id);
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Tile image missing for ID: " + this.id + ", loading placeholder.");
            InputStream is = getClass().getResourceAsStream("/images/placeholder.png");
            if (is == null) {
                throw new RuntimeException("Placeholder image missing in /images/tiles/placeholder.png");
            }
            return new Image(is);
        }
    }

    /**
     * Returns the rotation angle of the tile in degrees.
     * @return the tile rotation in degrees (multiples of 90)
     */
    public int getRotation() {
        return this.rotation * 90;
    }

    /**
     * Returns the list of goods currently present on the tile.
     * @return a list of goods (as strings)
     */
    public List<String> getGoods() {
        return goods;
    }

    /**
     * Returns the list of crew tokens currently present on the tile.
     * @return a list of tokens (as strings)
     */
    public List<String> getTokens() {
        return tokens;
    }
}
