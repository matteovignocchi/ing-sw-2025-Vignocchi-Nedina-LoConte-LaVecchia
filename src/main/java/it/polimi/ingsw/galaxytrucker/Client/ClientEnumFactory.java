package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

//  UNICA CLASSE ELIMINABILE A MIO AVVISO , MA MAGARI SI PUO SFRUTTARE PER QUALCOSA POI QUINDI RIMANE QUI

public class ClientEnumFactory {
    ObjectMapper mapper = new ObjectMapper();


    public  String getColour(String colourName) {
        return switch (colourName.toUpperCase()) {
            case "RED" -> "RED";
            case "BLUE" -> "BLUE";
            case "GREEN" -> "GREEN";
            case "YELLOW" -> "YELLOW";
            default -> "BOH";
        };
    }

    public ClientGamePhase describeGamePhase(String jsonString) {
        try {
            String clean = mapper.readValue(jsonString, String.class);  // rimuove le virgolette
            return ClientGamePhase.valueOf(clean.toUpperCase());
        } catch (Exception e) {
            System.err.println("Errore nella deserializzazione GamePhase: " + e.getMessage());
            return ClientGamePhase.EXIT; // valore di fallback o lancia eccezione se preferisci
        }
    }
    // Esempio: conversione lista colori in simboli
    public String renderColourList(List<String> colours) {
        String acc = "";
        for (String colour : colours) {
            String colourSymbol = getColour(colour);
            acc = acc + " " + colourSymbol;
        }
        return acc;
    }


}

