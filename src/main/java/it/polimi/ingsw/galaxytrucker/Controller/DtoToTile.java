package it.polimi.ingsw.galaxytrucker.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.*;
import it.polimi.ingsw.galaxytrucker.TileDTO;

public class DtoToTile {
    public Tile fromDTO(TileDTO dto) {
        Tile tile = switch (dto.type.toUpperCase()) {
            case "ENGINE" -> new Engine(dto.a, dto.b, dto.c, dto.d, dto.idDouble, dto.id);
            case "CANNON" -> new Cannon(dto.a, dto.b, dto.c, dto.d, dto.idDouble, dto.id);
            case "ENERGYCELL" -> new EnergyCell(dto.a, dto.b, dto.c, dto.d, dto.max, dto.id);
            case "MULTIJOINT" -> new MultiJoint(dto.a, dto.b, dto.c, dto.d, dto.id);
            case "STORAGEUNIT" -> {
                StorageUnit s = new StorageUnit(dto.a, dto.b, dto.c, dto.d, dto.max, dto.advance, dto.id);
                for (String good : dto.goods) {
                    s.getListOfGoods().add(Colour.valueOf(good));
                }
                yield s;
            }
            case "HOUSINGUNIT" -> {
                HousingUnit h = new HousingUnit(dto.a, dto.b, dto.c, dto.d, Human.valueOf(dto.human), dto.id);
                h.setSize(dto.max);
                for (String token : dto.tokens) {
                    h.getListOfToken().add(Human.valueOf(token));
                }
                yield h;
            }
            case "SHIELD" -> {
                Shield s = new Shield(dto.a, dto.b, dto.c, dto.d, dto.id);
                // protectedCorners è già impostato nel costruttore, opzionale qui
                yield s;
            }
            case "EMPTYSPACE" -> new EmptySpace();
            default -> throw new IllegalArgumentException("Tipo di tile sconosciuto: " + dto.type);
        };
        return tile;
    }

}
