package it.polimi.ingsw.galaxytrucker.Client;

import java.util.List;

//  UNICA CLASSE ELIMINABILE A MIO AVVISO , MA MAGARI SI PUO SFRUTTARE PER QUALCOSA POI QUINDI RIMANE QUI

public class ClientEnumFactory {

    public  String getColour(String colourName) {
        return switch (colourName.toUpperCase()) {
            case "RED" -> "RED";
            case "BLUE" -> "BLUE";
            case "GREEN" -> "GREEN";
            case "YELLOW" -> "YELLOW";
            default -> "BOH";
        };
    }

    public String describeGamePhase(String phaseName) {
        return phaseName.toUpperCase();
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

