package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("rotation")
    public int rotation;

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

    public int getRotation() {
        return this.rotation * 90;
    }

    public int getRotationIndex() {
        return rotation;
    }

    public void setRotationIndex(int rotation) {
        this.rotation = rotation;
    }

    public List<String> getGoods() {
        return goods;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public int getNumHumans() {
        // Conta i token "BASIC" (o altro filtro che usi per "umano")
        return (int) tokens.stream().filter(t -> t.equalsIgnoreCase("BASIC")).count();
    }

    public int getNumBatteries() {
        return (int) tokens.stream().filter(t -> t.equalsIgnoreCase("BATTERY")).count();
    }

    public boolean hasPurpleAlien() {
        return tokens.contains("PURPLE");
    }

    public boolean hasBrownAlien() {
        return tokens.contains("BROWN");
    }



}
