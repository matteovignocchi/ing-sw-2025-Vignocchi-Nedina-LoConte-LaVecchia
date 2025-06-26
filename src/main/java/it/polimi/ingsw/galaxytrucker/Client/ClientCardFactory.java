package it.polimi.ingsw.galaxytrucker.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for creating ClientCard objects from JSON strings.
 *
 * Provides methods to deserialize individual cards and lists of cards
 * received from the server.
 * @author Oleg Nedina
 */
public class ClientCardFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Deserializes a JSON string into a ClientCard object.
     * @param json the JSON representation of a single card
     * @return the deserialized ClientCard
     * @throws IOException if the JSON is malformed or cannot be parsed
     */
    public ClientCard fromJson(String json) throws IOException {
        return mapper.readValue(json, ClientCard.class);
    }

    /**
     * Deserializes a JSON array string into a list of ClientCard objects.
     * @param jsonArray the JSON array string representing multiple cards
     * @return a list of deserialized ClientCard instances
     * @throws IOException if the JSON is malformed or cannot be parsed
     */
    public List<ClientCard> fromJsonList(String jsonArray) throws IOException {
        return Arrays.asList(mapper.readValue(jsonArray, ClientCard[].class));
    }

}
