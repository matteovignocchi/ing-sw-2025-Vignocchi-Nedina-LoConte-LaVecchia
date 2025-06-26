package it.polimi.ingsw.galaxytrucker.Controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.DtoConvention.TileDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class responsible for converting Tile objects to their DTO representation
 * and serializing/deserializing them as JSON.
 * Supports both individual tiles and full dashboard matrices. Used to enable communication
 * between model logic and client-side rendering.
 * @author Oleg Nedina
 */
public class TileSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Converts a Tile object into its corresponding TileDTO.
     * This method detects the concrete type of the tile and maps all relevant attributes
     * (connectors, contents, rotation, etc.) into a DTO suitable for transfer or serialization.
     * @param tile the Tile to convert
     * @return the resulting TileDTO
     */
    public TileDTO toDTO(Tile tile) {
        TileDTO dto = new TileDTO();
        dto.a = tile.controlCorners(0);
        dto.b = tile.controlCorners(1);
        dto.c = tile.controlCorners(2);
        dto.d = tile.controlCorners(3);
        dto.id = tile.getIdTile();
        dto.rotation = tile.rotation;
        dto.type = tile.getClass().getSimpleName().toUpperCase();
        dto.idDouble = false;
        dto.advance = false;
        dto.max = 0;
        dto.capacity = 0;
        dto.tokens = List.of();
        dto.goods = List.of();
        dto.protectedCorners = new ArrayList<>();
        switch (tile) {
            case Engine e -> {
                dto.type = "ENGINE";
                dto.idDouble = e.isDouble();
            }

            case Cannon c -> {
                dto.type = "CANNON";
                dto.idDouble = c.isDouble();
            }

            case StorageUnit s -> {
                dto.type = "STORAGEUNIT";
                dto.advance = s.isAdvanced();
                dto.max = s.getMax();
                dto.goods = s.getListOfGoods() != null ?
                        s.getListOfGoods().stream().map(Enum::name).toList() :
                        List.of();
            }

            case HousingUnit h -> {
                dto.type = "HOUSINGUNIT";
                dto.human = h.getType() != null ? h.getType().name() : null;
                dto.max = h.returnLenght();
                dto.tokens = h.getListOfToken() != null ?
                        h.getListOfToken().stream().map(Enum::name).toList() :
                        List.of();
            }

            case Shield s -> {
                dto.type = "SHIELD";
                for (int i = 0; i < 4; i++) {
                    dto.protectedCorners.add(s.getProtectedCorner(i));
                }
            }

            case EnergyCell ec -> {
                dto.type = "ENERGYCELL";
                dto.capacity = ec.getCapacity();
            }
            default -> {}
        }
        return dto;
    }

    /**
     * Converts a Tile object to a JSON string.
     * The tile is first converted to a TileDTO, then serialized using Jackson.
     * @param tile the tile to serialize
     * @return the JSON string representation of the tile
     * @throws JsonProcessingException if the serialization fails
     */
    public String toJson(Tile tile) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(tile));
    }


    /**
     * Converts a list of Tile objects into a JSON array string.
     * Each tile is converted to a TileDTO before being serialized.
     * @param tiles the list of tiles to serialize
     * @return a JSON string representing the list
     * @throws JsonProcessingException if serialization fails
     */
    public String toJsonList(List<Tile> tiles) throws JsonProcessingException {
        List<TileDTO> dtos = new ArrayList<>();
        for (Tile tile : tiles) {
            TileDTO dto = toDTO(tile);
            dtos.add(dto);
        }
        return mapper.writeValueAsString(dtos);
    }

    /**
     * Converts a 2D matrix of Tile objects into a matrix of JSON strings.
     * Useful for sending the full dashboard or ship layout to the client.
     * @param matrix the tile matrix to serialize
     * @return a matrix of JSON strings
     * @throws JsonProcessingException if serialization fails
     */
    public String[][] toJsonMatrix(Tile[][] matrix) throws JsonProcessingException {
        int rows = matrix.length;
        int cols = matrix[0].length;
        String[][] jsonMatrix = new String[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                jsonMatrix[i][j] = toJson(matrix[i][j]);
        return jsonMatrix;
    }

    /**
     * Deserializes a JSON string into a Tile object.
     * The method converts the JSON into a TileDTO, then reconstructs the corresponding Tile model.
     * @param json the JSON string to parse
     * @return the reconstructed Tile
     * @throws JsonProcessingException if deserialization fails
     */
    public Tile fromJson(String json) throws JsonProcessingException {
        TileDTO dto = mapper.readValue(json, TileDTO.class);
        return fromDTO(dto);
    }

    /**
     * Converts a TileDTO into the corresponding Tile model object.
     * Instantiates the correct subclass based on the "type" field and fills in
     * properties such as connectors, goods, tokens, and rotation.
     * @param dto the TileDTO to convert
     * @return the reconstructed Tile model object
     * @throws IllegalArgumentException if the type is unknown
     */
    public Tile fromDTO(TileDTO dto) {
        Tile tile = switch (dto.type.toUpperCase()) {
            case "ENGINE" -> new Engine(dto.a, dto.b, dto.c, dto.d, dto.idDouble, dto.id);
            case "CANNON" -> new Cannon(dto.a, dto.b, dto.c, dto.d, dto.idDouble, dto.id);
            case "ENERGYCELL" -> new EnergyCell(dto.a, dto.b, dto.c, dto.d, dto.capacity, dto.id);
            case "MULTIJOINT" -> new MultiJoint(dto.a, dto.b, dto.c, dto.d, dto.id);
            case "STORAGEUNIT" -> {
                StorageUnit su = new StorageUnit(dto.a, dto.b, dto.c, dto.d, dto.max, dto.advance, dto.id);
                if (dto.goods != null)
                    for (String g : dto.goods)
                        su.addGood(Colour.valueOf(g));
                yield su;
            }
            case "HOUSINGUNIT" -> {
                HousingUnit hu = new HousingUnit(dto.a, dto.b, dto.c, dto.d, Human.valueOf(dto.human), dto.id);
                hu.setSize(dto.max);
                if (dto.tokens != null)
                    for (String tok : dto.tokens) {
                        try {
                            hu.addHuman(Human.valueOf(tok));
                        } catch (BusinessLogicException e) {
                            throw new RuntimeException(e);
                        }
                    }
                yield hu;
            }
            case "SHIELD" -> {
                Shield s = new Shield(dto.a, dto.b, dto.c, dto.d, dto.id);
                for (int i = 0; i < 4 && i < dto.protectedCorners.size(); i++)
                    s.setProtectedCorner(i, dto.protectedCorners.get(i));
                yield s;
            }
            case "EMPTYSPACE" -> new EmptySpace();
            default -> throw new IllegalArgumentException("Unknown tile type: " + dto.type);
        };
        tile.rotation = dto.rotation;
        return tile;
    }
}
