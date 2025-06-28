package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class CardGeneration implements Serializable {
    private final List<Card> level1Cards;
    private final List<Card> level2Cards;
    private final List<Card> demoCards;

    /**
     * Creates a CardGeneration instance and loads all adventure cards from predefined JSON files.
     * The cards are divided into three categories: Level 1 cards, Level 2 cards, and Demo cards.
     * Each category is populated by parsing the corresponding JSON file using Jackson.
     *
     * @throws IOException if one of the specified card files cannot be found or read correctly.
     */

    public CardGeneration() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        level1Cards = loadCards("cards_level1_data.json", mapper);
        level2Cards = loadCards("cards_level2_data.json", mapper);
        demoCards = loadCards("cards_demo_data.json", mapper);
    }

    /**
     * Loads a list of cards from the specified JSON file using the specified ObjectMapper.
     * The method reads the JSON as a resource from the classpath and deserializes it into a list of card instances.
     * The structure of the JSON must match the types registered in the Card interface using Jackson annotations.
     *
     * @param fileName the name of the JSON file (relative to the classpath).
     * @param mapper the Jackson Object Mapper used for deserialization.
     * @return a list of cards read from the file.
     * @throws IOException if the file is not found or if deserialization fails.
     */

    public List<Card> loadCards(String fileName, ObjectMapper mapper) throws IOException {
        try (InputStream file = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if(file ==null) throw new IOException("File not found" + fileName);
            return mapper.readValue(file, new TypeReference<>() {});
        }
    }

    /**
     * @return the list of level 1 cards.
     */

    public List<Card> getLevel1Cards() {return level1Cards;}

    /**
     * @return the list of level 2 cards.
     */

    public List<Card> getLevel2Cards() {return level2Cards;}

    /**
     * @return the list of demo cards for the test flight.
     */

    public List<Card> getDemoCards() {return demoCards;}
}