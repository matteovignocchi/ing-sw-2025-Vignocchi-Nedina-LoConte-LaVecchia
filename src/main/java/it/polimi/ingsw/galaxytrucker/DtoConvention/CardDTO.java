package it.polimi.ingsw.galaxytrucker.DtoConvention;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import java.util.List;

/**
 * Public DTO (Data Transfer Object) used to represent a game card and its effects.
 * This object is used to serialize and transfer card data between client and server.
 * It includes both descriptive and mechanical fields needed during gameplay.
 * Fields:
 * - type: the type of card (e.g. encounter, station, etc.)
 * - idCard: unique identifier of the card
 * - days: number of days (e.g. delay or effect duration)
 * - credits: number of credits gained or lost
 * - firePower: firepower value required or affected
 * - numCrewmates: number of crewmates involved
 * - numGoods: number of goods referenced
 * - stationGoods: list of goods available in a station (if applicable)
 * - rewardGoods: flat reward goods from the card
 * - rewardGoodsList: list of goods grouped by choice or effect options
 * - directions: list of directions involved in the effect (used in attacks)
 * - sizes: list of booleans indicating sizes (e.g. for meteors: small/large)
 * @author Olen Nedina
 */
public class CardDTO {
    public String type;
    public String idCard;
    public Integer days;
    public Integer credits;
    public Integer firePower;
    public Integer numCrewmates;
    public Integer numGoods;

    public List<Colour> stationGoods;
    public List<Colour> rewardGoods;
    public List<List<Colour>> rewardGoodsList;

    public List<Integer> directions;
    public List<Boolean> sizes;
}