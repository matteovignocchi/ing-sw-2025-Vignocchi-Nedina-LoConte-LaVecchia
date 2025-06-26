package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.DtoConvention.TileDTO;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for converting ClientTile objects to and from JSON.
 * Provides methods to deserialize single tiles, tile lists, and tile matrices,
 * as well as convert tiles to DTOs for server communication or internal use.
 * @author Oleg Nedina
 */
public class ClientTileFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Factory class for converting ClientTile objects to and from JSON.
     * Provides methods to deserialize single tiles, tile lists, and tile matrices,
     * as well as convert tiles to DTOs for server communication or internal use.
     */
    public  ClientTile fromJson(String json) throws IOException {
        return mapper.readValue(json, ClientTile.class);
    }

    /**
     * Deserializes a JSON array string into a list of ClientTile objects.
     * @param jsonList the JSON array string representing multiple tiles
     * @return a list of ClientTile instances
     * @throws IOException if the JSON is invalid or cannot be parsed
     */
    public  List<ClientTile> fromJsonList(String jsonList) throws IOException {
        return Arrays.asList(mapper.readValue(jsonList, ClientTile[].class));
    }

    /**
     * Deserializes a 2D matrix of JSON tile strings into a matrix of ClientTile objects.
     * @param matrix a 2D array of JSON strings
     * @return a matrix of deserialized ClientTile objects
     * @throws IOException if any tile string is invalid
     */
    public  ClientTile[][] fromJsonMatrix(String[][] matrix) throws IOException {
        ClientTile[][] result = new ClientTile[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                result[i][j] = fromJson(matrix[i][j]);
        return result;
    }

    /**
     * Converts a ClientTile into a TileDTO.
     * This is useful for sending tile data back to the server or through a shared format.
     * @param tile the ClientTile to convert
     * @return the corresponding TileDTO
     */
    public TileDTO toDTO(ClientTile tile) {
        TileDTO dto = new TileDTO();
        dto.type = tile.type;
        dto.a = tile.a;
        dto.b = tile.b;
        dto.c = tile.c;
        dto.d = tile.d;
        dto.id = tile.id;
        dto.idDouble = tile.idDouble;
        dto.advance = tile.advance;
        dto.max = tile.max;
        dto.human = tile.human;
        dto.goods = tile.goods;
        dto.tokens = tile.tokens;
        dto.protectedCorners = tile.protectedCorners;
        dto.capacity = tile.capacity;
        dto.rotation = tile.rotation;
        return dto;
    }

    /**
     * Serializes a ClientTile to a JSON string.
     * The tile is first converted to a DTO before being serialized.
     * @param tile the tile to serialize
     * @return the JSON representation of the tile
     * @throws JsonProcessingException if serialization fails
     */
    public  String toJson(ClientTile tile) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(tile));
    }

}
