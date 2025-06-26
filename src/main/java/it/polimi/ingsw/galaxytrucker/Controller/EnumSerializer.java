package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.GamePhase;
import java.util.List;

/**
 * Utility class for serializing enum values used in the game model.
 * Provides methods to convert Colour and GamePhase enums into string representations,
 * typically for JSON communication between client and server.
 * @author Olen Nedina
 */
public class EnumSerializer {
    private  final ObjectMapper mapper = new ObjectMapper();

    /**
     * Serializes a Colour enum value into a JSON string.
     * @param colour the Colour to serialize
     * @return the JSON string representing the enum name
     * @throws JsonProcessingException if serialization fails
     */
    public  String serializeColour(Colour colour) throws JsonProcessingException {
        return mapper.writeValueAsString(colour.name());
    }

    /**
     * Serializes a GamePhase enum value into an uppercase JSON string.
     * @param phase the GamePhase to serialize
     * @return the uppercase JSON string representing the enum name
     * @throws JsonProcessingException if serialization fails
     */
    public  String serializeGamePhase(GamePhase phase) throws JsonProcessingException {
        return mapper.writeValueAsString(phase.name()).toUpperCase();
    }

    /**
     * Serializes a list of Colour enum values into a list of string names.
     * @param colours the list of Colour values to serialize
     * @return a list of string names corresponding to the enum values
     */
    public  List<String> serializeColoursList(List<Colour> colours) throws JsonProcessingException {
        return colours.stream().map(Enum::name).toList();
    }
}
