package it.polimi.ingsw.galaxytrucker.DtoConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Public DTO (Data Transfer Object) used to represent a ship tile.
 * This object is used to serialize and transfer tile data between client and server,
 * including structural properties, status, and content (goods, tokens, etc.).
 * Fields:
 * - type: the type of tile (e.g. ENGINE, CANNON, STORAGEUNIT, etc.)
 * - a, b, c, d: connector values on the four sides (N, E, S, W)
 * - id: unique identifier of the tile
 * - idDouble: true if the tile is a double version (e.g. double engine/cannon)
 * - max: maximum capacity (for goods or energy, depending on tile type)
 * - advance: true if the tile supports advanced features (e.g. accepts red goods)
 * - capacity: remaining energy capacity (used in EnergyCell tiles)
 * - human: the type of human/alien in a housing unit (e.g. HUMAN, PURPLE_ALIEN)
 * - goods: list of stored goods (as strings)
 * - tokens: list of crew tokens in the tile (used in HousingUnit)
 * - protectedCorners: list of sides protected by the tile (used in Shield)
 * - rotation: current rotation of the tile (0 to 3, clockwise)
 * @author Olen Nedina
 */
public class TileDTO {
    public String type;
    public int a, b, c, d;
    public int id;
    public boolean idDouble;
    public int max;
    public boolean advance;
    public int capacity;
    public String human;
    public List<String> goods = new ArrayList<>();
    public List<String> tokens = new ArrayList<>();
    public List<Integer> protectedCorners = new ArrayList<>();
    public int rotation;
}