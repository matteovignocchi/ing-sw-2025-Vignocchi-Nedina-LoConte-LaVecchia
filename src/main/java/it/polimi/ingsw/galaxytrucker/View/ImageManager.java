package it.polimi.ingsw.galaxytrucker.View;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class ImageManager {
    private static final Map<String, Image> images = new HashMap<>();

    // Abbozzo classe per precaricare tutte le immagini

    static {
        for (int i = 1; i <= 152; i++) {
            String id = String.format("tile_%03d", i);
//            String path = "/images/" + id + ".png"; devo mettere il path giusto
            String path = new String();
            try {
                images.put(id, new Image(ImageManager.class.getResourceAsStream(path)));
            } catch (Exception e) {
                System.err.println("Failed to load: " + path);
            }
        }
    }

}
