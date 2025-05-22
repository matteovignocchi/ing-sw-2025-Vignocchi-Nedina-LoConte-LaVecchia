package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Model.Tile.StorageUnit;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;

import java.util.ArrayList;
import java.util.List;

public class TileDTO {
    public String type;
    public int a, b, c, d;
    public int id;
    public boolean idDouble;
    public int max;
    public boolean advance;
    public String human;
    public List<String> goods = new ArrayList<>();
    public List<String> tokens = new ArrayList<>();
    public List<Integer> protectedCorners = new ArrayList<>();

    public TileDTO toDTO(Tile tile) {
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
                for (int i = 0; i < 4; i++) {
                    dto.protectedCorners.add(s.getProtectedCorner(i));
                }
            }
        }

        return dto;
    }
}