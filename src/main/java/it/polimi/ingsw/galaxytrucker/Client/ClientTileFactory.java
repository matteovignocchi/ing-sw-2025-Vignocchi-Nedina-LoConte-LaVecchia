package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.TileDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientTileFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public  ClientTile fromJson(String json) throws IOException {
        return mapper.readValue(json, ClientTile.class);
    }

    public  List<ClientTile> fromJsonList(String jsonList) throws IOException {
        return Arrays.asList(mapper.readValue(jsonList, ClientTile[].class));
    }

    public  ClientTile[][] fromJsonMatrix(String[][] matrix) throws IOException {
        ClientTile[][] result = new ClientTile[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                result[i][j] = fromJson(matrix[i][j]);
        return result;
    }

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
        return dto;
    }

    public  String toJson(ClientTile tile) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(tile));
    }

}
