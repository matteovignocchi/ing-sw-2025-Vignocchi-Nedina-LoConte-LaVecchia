package it.polimi.ingsw.galaxytrucker.Client;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.List;

/**
 * Client-side representation of a game card.
 *
 * Holds all display-related and gameplay-relevant information needed by the view layer,
 * including textual data and methods to load associated card images.
 * @author Oleg Nedina
 */
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



    /**
     * Returns the unique identifier of the card.
     * @return the card's ID
     */
    public String getIdCard() {
        return idCard;
    }

    /**
     * Loads the image corresponding to a specific card ID.
     * The card ID must follow the format "deck_cardNumber", e.g., "1_03".
     * Loads the appropriate image from the classpath based on the deck number.
     * @param cardId the ID of the card to load (e.g., "1_03")
     * @return the Image associated with the card
     * @throws RuntimeException if the ID format is invalid or the image is not found
     */
    public static Image loadImageById(String cardId) {
        try {
            String[] parts = cardId.split("_");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid cardId format: " + cardId);

            String deck = parts[0];
            String id = parts[1];

            String imagePath;
            switch (deck) {
                case "1" -> imagePath = "/images/Polytechnic/cards/GT-cards_I_IT_" + id + ".jpg";
                case "2" -> imagePath = "/images/Polytechnic/cards/GT-cards_II_IT_" + id + ".jpg";
                default -> throw new IllegalArgumentException("Invalid deck number in cardId: " + cardId);
            }

            InputStream is = ClientCard.class.getResourceAsStream(imagePath);
            if (is == null) throw new RuntimeException("Image not found at: " + imagePath);
            return new Image(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load image for ID: " + cardId, e);
        }
    }

    /**
     * Returns the image associated with this card.
     * If the image cannot be found, a fallback placeholder image is returned.
     * @return the Image object for the card
     * @throws RuntimeException if both the card image and placeholder are missing
     */
    public Image getImage() {
        try {
            return loadImageById(this.idCard);
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Image not found for card ID: " + this.idCard + ", using placeholder.");
            InputStream is = getClass().getResourceAsStream("/images/placeholder.png");
            if (is == null) {
                throw new RuntimeException("Missing placeholder image in /placeholder.png");
            }
            return new Image(is);
        }
    }
}
