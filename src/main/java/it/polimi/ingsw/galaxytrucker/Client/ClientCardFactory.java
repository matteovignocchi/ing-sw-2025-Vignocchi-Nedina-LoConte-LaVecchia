package it.polimi.ingsw.galaxytrucker.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClientCardFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public ClientCard fromJson(String json) throws IOException {
        return mapper.readValue(json, ClientCard.class);
    }

    public static List<ClientCard> fromJsonList(String jsonArray) throws IOException {
        return Arrays.asList(mapper.readValue(jsonArray, ClientCard[].class));
    }

}
