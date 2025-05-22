package it.polimi.ingsw.galaxytrucker.Client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTileFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ClientTile fromJson(String json) throws IOException {
        return mapper.readValue(json, ClientTile.class);
    }

    public static List<ClientTile> fromJsonList(List<String> jsonList) throws IOException {
        List<ClientTile> list = new ArrayList<>();
        for (String json : jsonList)
            list.add(fromJson(json));
        return list;
    }

    public static ClientTile[][] fromJsonMatrix(String[][] matrix) throws IOException {
        ClientTile[][] result = new ClientTile[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                result[i][j] = fromJson(matrix[i][j]);
        return result;
    }
}
