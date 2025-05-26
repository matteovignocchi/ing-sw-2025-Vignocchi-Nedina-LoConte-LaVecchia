package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.GamePhase;

import java.util.List;

public class EnumSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serializeColour(Colour colour) throws JsonProcessingException {
        return mapper.writeValueAsString(colour.name());
    }

    public static String serializeGamePhase(GamePhase phase) throws JsonProcessingException {
        return mapper.writeValueAsString(phase.name());
    }

    public static String serializeColoursList(List<Colour> colours) throws JsonProcessingException {
        List<String> names = colours.stream().map(Enum::name).toList();
        return mapper.writeValueAsString(names);
    }
}
