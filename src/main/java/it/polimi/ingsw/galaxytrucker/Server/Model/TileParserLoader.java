package it.polimi.ingsw.galaxytrucker.Server.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Server.Model.Tile.*;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;

public class TileParserLoader {
    public static List<Tile> loadTiles(String filename) {
        List<Tile> pileOfTile = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(new File(filename));
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
                                obj.get("d").asInt()
                        );
                        break;

                    case "STORAGEUNIT":
                        tile = new StorageUnit(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("max").asInt(),
                                obj.get("advance").asBoolean()
                        );
                        break;

                    case "ENGINE":
                        tile = new Engine(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("idDouble").asBoolean()
                        );
                        break;

                    case "CANNON":
                        tile = new Cannon(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("idDouble").asBoolean()
                        );
                        break;

                    case "HOUSINGUNIT":
                        tile = new HousingUnit(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                Human.valueOf(obj.get("human").asText())
                        );
                        break;

                    case "SHIELD":
                        tile = new Shield(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt()
                        );
                        break;

                    case "ENERGYCELL":
                        tile = new EnergyCell(
                                obj.get("a").asInt(),
                                obj.get("b").asInt(),
                                obj.get("c").asInt(),
                                obj.get("d").asInt(),
                                obj.get("max").asInt()
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




