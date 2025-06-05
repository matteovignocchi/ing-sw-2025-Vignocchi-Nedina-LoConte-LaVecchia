package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.DtoConvention.TileDTO;

import java.util.ArrayList;
import java.util.List;

public class TileSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public TileDTO toDTO(Tile tile) {
        TileDTO dto = new TileDTO();
        dto.type = tile.getClass().getSimpleName().toUpperCase();
        dto.a = tile.controlCorners(0);
        dto.b = tile.controlCorners(1);
        dto.c = tile.controlCorners(2);
        dto.d = tile.controlCorners(3);
        dto.id = tile.getIdTile();

        switch (dto.type) {
            case "ENGINE" -> dto.idDouble = ((Engine) tile).isDouble();
            case  "CANNON" -> dto.idDouble = ((Cannon) tile).isDouble();
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
            case "ENERGYCELL" -> {
                EnergyCell tmp = (EnergyCell) tile;
                dto.capacity = tmp.getCapacity();
            }
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
    private Tile fromDTO(TileDTO dto) {
        // per leggibilità
        int a = dto.a, b = dto.b, c = dto.c, d = dto.d, id = dto.id;

        switch (dto.type) {
            case "EMPTYSPACE" -> {
                return new EmptySpace();                          // id 157 già fissato nel costruttore
            }
            case "ENGINE" -> {
                return new Engine(a, b, c, d, dto.idDouble, id);  // (a,b,c,d,isDouble,id)
            }
            case "CANNON" -> {
                return new Cannon(a, b, c, d, dto.idDouble, id);  // (a,b,c,d,isDouble,id)
            }
            case "MULTIJOINT" -> {
                return new MultiJoint(a, b, c, d, id);            // (a,b,c,d,id)
            }
            case "ENERGYCELL" -> {
                return new EnergyCell(a, b, c, d, dto.capacity, id);   // (a,b,c,d,capacity,id)
            }
            case "SHIELD" -> {
                Shield s = new Shield(a, b, c, d, id);            // costruttore base
                // copia vettore protectedCorners dal DTO
                for (int i = 0; i < 4 && i < dto.protectedCorners.size(); i++)
                    s.setProtectedCorner(i, dto.protectedCorners.get(i));
                return s;
            }
            case "STORAGEUNIT" -> {
                StorageUnit su = new StorageUnit(a, b, c, d, dto.max, dto.advance, id);
                if (dto.goods != null) {
                    for (String gStr : dto.goods) {
                        Colour colour = Colour.valueOf(gStr);
                        try {
                            su.addGood(colour);
                        } catch (Exception ignored) {
                        }
                    }
                }
                return su;
            }
            case "HOUSINGUNIT" -> {
                Human hType = Human.valueOf(dto.human);
                HousingUnit hu = new HousingUnit(a, b, c, d, hType, id);
                hu.setSize(dto.max);          // max capacity
                if (dto.tokens != null) {
                    for (String tok : dto.tokens) {
                        Human token = Human.valueOf(tok);
                        try {
                            hu.addHuman(token);
                        } catch (Exception ignored) {
                        }
                    }
                }
                return hu;
            }
            default -> throw new IllegalArgumentException("Tipo di tile sconosciuto: " + dto.type);
        }
    }
}
