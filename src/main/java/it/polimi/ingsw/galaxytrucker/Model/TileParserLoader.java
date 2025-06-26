package it.polimi.ingsw.galaxytrucker.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility class responsible for loading and parsing tile data from a JSON file.
 * This parser reads the {@code tile_data.json} file from the classpath and
 * constructs the corresponding {@link Tile} objects based on the specified type.
 * It supports all tile subclasses defined in the model.
 * @author Matteo Vignocchi
 */

public class TileParserLoader implements Serializable {

    /**
     * Loads the list of tiles from the JSON resource file.
     * <p>
     * Parses each tile definition from {@code tile_data.json} and instantiates the
     * appropriate {@link Tile} subclass based on the "type" field.
     * @return a list of {@link Tile} objects parsed from the JSON file
     * @throws RuntimeException if the JSON resource is not found
     */

    public List<Tile> loadTiles() {
        List<Tile> pileOfTile = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("tile_data.json")){
            if (is == null) {
                throw new RuntimeException("File 'tiles_data.json' non trovato nel classpath!");
            }
            JsonNode root = mapper.readTree(is);
            JsonNode tilesArray = root.get("tiles");
            for (JsonNode obj : tilesArray) {
                String type = obj.get("type").asText();
                Tile tile = null;
                switch (type) {
                    case "MULTIJOINT":
                        tile = new MultiJoint(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("id").asInt()
                        );
                        break;

                    case "STORAGEUNIT":
                        tile = new StorageUnit(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("max").asInt(),
                                obj.get("advance").asBoolean(),
                                obj.get("id").asInt()
                        );
                        break;

                    case "ENGINE":
                        tile = new Engine(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("idDouble").asBoolean(),
                                obj.get("id").asInt()
                        );
                        break;

                    case "CANNON":
                        tile = new Cannon(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("idDouble").asBoolean(),
                                obj.get("id").asInt()
                        );
                        break;

                    case "HOUSINGUNIT":
                        tile = new HousingUnit(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                Human.valueOf(obj.get("human").asText()),
                                obj.get("id").asInt()
                        );
                        break;

                    case "SHIELD":
                        tile = new Shield(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("id").asInt()
                        );
                        break;

                    case "ENERGYCELL":
                        tile = new EnergyCell(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("max").asInt(),
                                obj.get("id").asInt()
                        );
                        break;

                    default:
                        System.err.println("Unknown type: " + type);
                }
                if (tile != null) {
                    pileOfTile.add(tile);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return pileOfTile;
    }
}




