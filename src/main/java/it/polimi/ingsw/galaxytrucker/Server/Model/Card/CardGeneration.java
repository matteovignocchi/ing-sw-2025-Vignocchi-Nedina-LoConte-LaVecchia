package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CardGeneration {
    private final List<Card> level1Cards;
    private final List<Card> level2Cards;
    private final List<Card> demoCards;

    public CardGeneration() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        level1Cards = loadCards("cards_level1_data.json", mapper); //verificare che il path sia corretto
        level2Cards = loadCards("cards_level2_data.json", mapper);
        demoCards = loadCards("cards_demo_data.json", mapper);
    }

    private List<Card> loadCards(String fileName, ObjectMapper mapper) throws IOException {
        try (InputStream file = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if(file ==null) throw new IOException("File not found" + fileName);
            return mapper.readValue(file, new TypeReference<List<Card>>() {});
        }
    }

    //dobbiamo ritornare nuove liste?
    public List<Card> getLevel1Cards() {return level1Cards;}

    public List<Card> getLevel2Cards() {return level2Cards;}

    public List<Card> getDemoCards() {return demoCards;}
}