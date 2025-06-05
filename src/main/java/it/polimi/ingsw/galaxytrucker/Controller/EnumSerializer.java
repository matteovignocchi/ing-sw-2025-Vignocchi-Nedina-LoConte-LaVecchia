package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;

import java.util.List;

public class EnumSerializer {
    private  final ObjectMapper mapper = new ObjectMapper();

    public  String serializeColour(Colour colour) throws JsonProcessingException {
        return mapper.writeValueAsString(colour.name());
    }

    public  String serializeGamePhase(GamePhase phase) throws JsonProcessingException {
        return mapper.writeValueAsString(phase.name()).toUpperCase();
    }

    public  List<String> serializeColoursList(List<Colour> colours) throws JsonProcessingException {
        return colours.stream().map(Enum::name).toList();
    }
}
