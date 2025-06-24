package it.polimi.ingsw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.Model.TileParserLoader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TileParserLoaderTest {

    private final TileParserLoader loader = new TileParserLoader();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testLoadTilesNotNullOrEmpty() throws Exception {
        List<Tile> tiles = loader.loadTiles();
        assertNotNull(tiles);
        assertFalse(tiles.isEmpty(), "Il JSON principale deve produrre almeno una tile");
    }

    @Test
    void testLoadedCountMatchesJson() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("tile_data.json")) {
            assertNotNull(is);
            JsonNode root = mapper.readTree(is);
            int expected = root.get("tiles").size();
            assertEquals(expected, loader.loadTiles().size());
        }
    }

    @Test
    void testAllExpectedTypesPresent() {
        List<Tile> tiles = loader.loadTiles();
        Map<Class<? extends Tile>, Long> counts = tiles.stream().collect(Collectors.groupingBy(Tile::getClass, Collectors.counting()));
        assertTrue(counts.containsKey(MultiJoint.class));
        assertTrue(counts.containsKey(StorageUnit.class));
        assertTrue(counts.containsKey(Engine.class));
        assertTrue(counts.containsKey(Cannon.class));
        assertTrue(counts.containsKey(HousingUnit.class));
        assertTrue(counts.containsKey(Shield.class));
        assertTrue(counts.containsKey(EnergyCell.class));
    }

    @Test
    void testTileFieldsReflectJson() {
        List<Tile> tiles = loader.loadTiles();
        assertFalse(tiles.isEmpty());
        Tile t = tiles.get(0);
        assertTrue(t.getIdTile() >= 0);
        for (int side = 0; side < 4; side++) {
            int c = t.controlCorners(side);
            assertTrue(c >= 0 && c <= 7);
        }
    }
}

