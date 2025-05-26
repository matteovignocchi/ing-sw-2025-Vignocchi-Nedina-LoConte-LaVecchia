package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.TileDTO;

import java.util.ArrayList;
import java.util.List;

public class TileSerializer {

    private  final ObjectMapper mapper = new ObjectMapper();
    public  TileDTO toDTO(Tile tile) {
        TileDTO dto = new TileDTO();
        dto.type = tile.getClass().getSimpleName().toUpperCase();
        dto.a = tile.controlCorners(0);
        dto.b = tile.controlCorners(1);
        dto.c = tile.controlCorners(2);
        dto.d = tile.controlCorners(3);
        dto.id = tile.getIdTile();

        switch (dto.type) {
            case "ENGINE", "CANNON" -> dto.idDouble = ((Engine) tile).isDouble();
            case "STORAGEUNIT" -> {
                StorageUnit s = (StorageUnit) tile;
                dto.advance = s.isAdvanced();
                dto.max = s.getMax();
                dto.goods = s.getListOfGoods().stream().map(Enum::name).toList();
            }
            case "HOUSINGUNIT" -> {
                HousingUnit h = (HousingUnit) tile;
                dto.human = h.getType().name();
                dto.max = h.returnLenght();
                dto.tokens = h.getListOfToken().stream().map(Enum::name).toList();
            }
            case "SHIELD" -> {
                Shield s = (Shield) tile;
                for (int i = 0; i < 4; i++) dto.protectedCorners.add(s.getProtectedCorner(i));
            }
        }
        return dto;
    }

    public  String toJson(Tile tile) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(tile));
    }

    public  String toJsonList(List<Tile> tiles) throws JsonProcessingException {
        List<TileDTO> dtos = new ArrayList<>();
        for (Tile tile : tiles) {
            TileDTO dto = toDTO(tile);
            dtos.add(dto);
        }
        return mapper.writeValueAsString(dtos);
    }

    public  String[][] toJsonMatrix(Tile[][] matrix) throws JsonProcessingException {
        int rows = matrix.length;
        int cols = matrix[0].length;
        String[][] jsonMatrix = new String[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                jsonMatrix[i][j] = toJson(matrix[i][j]);
        return jsonMatrix;
    }
}
