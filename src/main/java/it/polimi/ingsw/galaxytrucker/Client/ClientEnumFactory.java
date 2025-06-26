package it.polimi.ingsw.galaxytrucker.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * Utility class for handling enum-related conversions on the client side.
 *
 * Provides support for converting colour names and game phase strings
 * into usable formats for the client UI or game logic.
 *
 * Although currently minimal, this class is kept for potential future extensions.
 * @author Oleg Nedina
 */

public class ClientEnumFactory {
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Converts a raw colour name to a standardized enum-style string.
     * Supports RED, BLUE, GREEN, and YELLOW. Any unknown value returns "BOH".
     * @param colourName the raw colour string (case-insensitive)
     * @return the standardized colour string or "BOH" if unknown
     */
    public  String getColour(String colourName) {
        return switch (colourName.toUpperCase()) {
            case "RED" -> "RED";
            case "BLUE" -> "BLUE";
            case "GREEN" -> "GREEN";
            case "YELLOW" -> "YELLOW";
            default -> "BOH";
        };
    }

    /**
     * Parses a JSON string representing a game phase and converts it to a ClientGamePhase enum.
     * This method removes surrounding quotes and ensures the value is uppercase before parsing.
     * If the string is invalid or unrecognized, it returns ClientGamePhase.EXIT.
     * @param jsonString the JSON-encoded string representing a game phase
     * @return the corresponding ClientGamePhase, or EXIT if invalid
     */
    public ClientGamePhase describeGamePhase(String jsonString) {
        try {
            String clean = mapper.readValue(jsonString, String.class);  // rimuove le virgolette
            return ClientGamePhase.valueOf(clean.toUpperCase());
        } catch (Exception e) {
            return ClientGamePhase.EXIT;
        }
    }

    /**
     * Converts a list of colour names into a single formatted string.
     * Each colour is mapped using getColour(...) and appended to the result.
     * @param colours the list of colour names
     * @return a space-separated string of converted colour symbols
     */
    public String renderColourList(List<String> colours) {
        String acc = "";
        for (String colour : colours) {
            String colourSymbol = getColour(colour);
            acc = acc + " " + colourSymbol;
        }
        return acc;
    }
}

