package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    public void rotateRight() {
        int temp = d;
        d = c;
        c = b;
        b = a;
        a = temp;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(3);
            protectedCorners.set(3, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(0));
            protectedCorners.set(0, tmp);
        }
    }

    public void rotateLeft() {
        int temp = a;
        a = b;
        b = c;
        c = d;
        d = temp;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(0);
            protectedCorners.set(0, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(3));
            protectedCorners.set(3, tmp);
        }
    }

    public static Image loadImageById(int tileId) {
        try {
            String imagePath = "/Polytechnic/tiles/GT-new_tiles_16_for web" + tileId + ".jpg";
            InputStream is = Tile.class.getResourceAsStream(imagePath);

            if (is == null) {
                imagePath = "/Polytechnic/tiles/GT-new_tiles_16_for web157";
                is = Tile.class.getResourceAsStream(imagePath);

                if (is == null) {
                    throw new RuntimeException("Default tile image not found");
                }
            }

            return new Image(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tile image for ID: " + tileId, e);
        }
    }

    public Image getImage() {
        return loadImageById(this.id);
    }
}

