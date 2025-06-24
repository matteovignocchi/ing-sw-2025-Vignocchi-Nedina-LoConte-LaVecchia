package it.polimi.ingsw.galaxytrucker.Client;
import javafx.css.CssParser;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.List;


public class ClientCard {
    public String type;
    public String idCard;
    public Integer days;
    public Integer credits;
    public Integer firePower;
    public Integer numCrewmates;
    public Integer numGoods;

    public List<String> stationGoods;
    public List<String> rewardGoods;
    public List<List<String>> rewardGoodsList;

    public List<Integer> directions;
    public List<Boolean> sizes;

    public String getIdCard() {
        return idCard;
    }

    public static Image loadImageById(String cardId) {
        try {
            String[] parts = cardId.split("_");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid cardId format: " + cardId);

            String deck = parts[0];
            String id = parts[1];

            String imagePath;
            switch (deck) {
                case "1" -> imagePath = "/Polytechnic/cards/GT-cards_I_IT_" + id + ".jpg";
                case "2" -> imagePath = "/Polytechnic/cards/GT-cards_II_IT_" + id + ".jpg";
                default -> throw new IllegalArgumentException("Invalid deck number in cardId: " + cardId);
            }

            InputStream is = ClientCard.class.getResourceAsStream(imagePath);
            if (is == null) throw new RuntimeException("Image not found at: " + imagePath);
            return new Image(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load image for ID: " + cardId, e);
        }
    }

    public Image getImage() {
        try {
            return loadImageById(this.idCard);
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Image not found for card ID: " + this.idCard + ", using placeholder.");
            InputStream is = getClass().getResourceAsStream("/placeholder.png");
            if (is == null) {
                throw new RuntimeException("Missing placeholder image in /placeholder.png");
            }
            return new Image(is);
        }
    }
}
