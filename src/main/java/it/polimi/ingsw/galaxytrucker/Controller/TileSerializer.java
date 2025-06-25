package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.DtoConvention.TileDTO;

import java.util.ArrayList;
import java.util.List;

public class TileSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public TileDTO toDTO(Tile tile) {
        TileDTO dto = new TileDTO();

        // Informazioni comuni
        dto.a = tile.controlCorners(0);
        dto.b = tile.controlCorners(1);
        dto.c = tile.controlCorners(2);
        dto.d = tile.controlCorners(3);
        dto.id = tile.getIdTile();
        dto.rotation = tile.rotation;

        // Valori di default/null-safe
        dto.type = tile.getClass().getSimpleName().toUpperCase();
        dto.idDouble = false;
        dto.advance = false;
        dto.max = 0;
        dto.capacity = 0;
        dto.tokens = List.of();
        dto.goods = List.of();
        dto.protectedCorners = new ArrayList<>();

        // Switch dinamico su tipo reale della tile
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

            // eventuali altri tipi (Connector, Cabin, ecc.) possono essere aggiunti qui
        }
        System.out.println("[DEBUG] tile.class = " + tile.getClass().getName());

        if (tile instanceof HousingUnit h) {
            System.out.println("[DEBUG] TOKENS IN DTO: " + h.getListOfToken());

            dto.tokens = h.getListOfToken().stream().map(Enum::name).toList();
            dto.human = h.getType().name();
            dto.max = h.returnLenght();
        }

        if (tile instanceof StorageUnit s) {
            dto.goods = s.getListOfGoods().stream().map(Enum::name).toList();
            dto.max = s.getMax();
            dto.advance = s.isAdvanced();
        }

        if (tile instanceof EnergyCell ec) {
            dto.capacity = ec.getCapacity();
        }

        if (tile instanceof Cannon c) {
            dto.idDouble = c.isDouble();
        }

        if (tile instanceof Engine e) {
            dto.idDouble = e.isDouble();
        }

        if (tile instanceof Shield s) {
            for (int i = 0; i < 4; i++) dto.protectedCorners.add(s.getProtectedCorner(i));
        }


        return dto;
    }


    public String toJson(Tile tile) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(tile));
    }

    public String toJsonList(List<Tile> tiles) throws JsonProcessingException {
        List<TileDTO> dtos = new ArrayList<>();
        for (Tile tile : tiles) {
            TileDTO dto = toDTO(tile);
            dtos.add(dto);
        }
        return mapper.writeValueAsString(dtos);
    }

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
     * Deserializza JSON -> TileDTO -> Tile (model)
     */
    public Tile fromJson(String json) throws JsonProcessingException {
        TileDTO dto = mapper.readValue(json, TileDTO.class);
        return fromDTO(dto);
    }

    /**
     * Converte il DTO in una vera Tile del model
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
