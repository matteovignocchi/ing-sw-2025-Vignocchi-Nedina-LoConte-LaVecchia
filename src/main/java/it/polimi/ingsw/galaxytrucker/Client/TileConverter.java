package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.DtoConvention.TileDTO;

public class TileConverter {
    public static TileDTO toDTO(ClientTile tile) {
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
}
