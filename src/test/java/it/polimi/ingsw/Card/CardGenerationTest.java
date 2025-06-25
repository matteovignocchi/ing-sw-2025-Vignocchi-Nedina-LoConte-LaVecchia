package it.polimi.ingsw.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardGenerationTest {

    @Test
    void loadCards_nonexistentFile_throwsIOException() throws Exception {
        CardGeneration gen = Mockito.mock(CardGeneration.class, Mockito.CALLS_REAL_METHODS);
        ObjectMapper mapper = new ObjectMapper();

        IOException ex = assertThrows(IOException.class, () -> gen.loadCards("non_existent_file.json", mapper));
        assertTrue(ex.getMessage().contains("File not found"));
    }

    @Test
    void loadCards_emptyJson_returnsEmptyList() throws Exception {
        CardGeneration gen = Mockito.mock(CardGeneration.class, Mockito.CALLS_REAL_METHODS);
        ObjectMapper mapper = new ObjectMapper();

        var list = gen.loadCards("dummy.json", mapper);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void getLevel1Cards_notEmptyAfterConstruction() throws IOException {
        CardGeneration gen = new CardGeneration();
        List<Card> lvl1 = gen.getLevel1Cards();
        assertNotNull(lvl1,    "Level1 list should not be null");
        assertFalse(lvl1.isEmpty(), "Level1 list should not be empty");
    }

    @Test
    void getLevel2Cards_notEmptyAfterConstruction() throws IOException {
        CardGeneration gen = new CardGeneration();
        List<Card> lvl2 = gen.getLevel2Cards();
        assertNotNull(lvl2,    "Level2 list should not be null");
        assertFalse(lvl2.isEmpty(), "Level2 list should not be empty");
    }
}

