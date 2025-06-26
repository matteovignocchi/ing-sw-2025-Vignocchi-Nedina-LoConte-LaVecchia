package it.polimi.ingsw.galaxytrucker.Controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.DtoConvention.CardDTO;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting Card objects into their corresponding DTO representations
 * and serializing them to JSON format.
 * Used to transfer card information between server and client in a simplified, standardized structure.
 * @author Oleg Nedina
 */
public class CardSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Converts a specific Card instance into a CardDTO.
     * This method uses pattern matching to detect the card type and extract all relevant
     * data needed by the client.
     * @param card the Card object to convert
     * @return a CardDTO containing the serialized data of the card
     */
    public static CardDTO toDTO(Card card) {
        CardDTO dto = new CardDTO();
        switch (card) {
            case AbandonedShipCard x->{
                dto.type = "ABANDONEDSHIPCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.credits = x.getCredits();
                dto.numCrewmates = x.getNumCrewmates();
            }
            case AbandonedStationCard x ->{
                dto.type = "ABANDONEDSTATIONCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.numCrewmates = x.getNumCrewmates();
                dto.stationGoods =  x.getStationGoods();
            }
            case MeteoritesRainCard x ->{
                dto.type = "METEORITESRAINCARD";
                dto.idCard = x.getIdCard();
                dto.directions = x.getMeteorites_directions();
                dto.sizes = x.getMeteorites_size();
            }
            case PiratesCard x ->{
                dto.type = "PIRATESCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.credits = x.getCredits();
                dto.firePower = x.getFirePower();
                dto.directions = x.getShots_directions();
                dto.sizes = x.getShots_size();
            }
            case PlanetsCard x ->{
                dto.type = "PLANETSCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.rewardGoodsList = x.getRewardGoods();
            }
            case SlaversCard x ->{
                dto.type = "SLAVERSCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.credits = x.getCredits();
                dto.firePower = x.getFirePower();
                dto.numCrewmates = x.getNumCrewmates();
            }
            case SmugglersCard x ->{
                dto.type = "SMUGGLERSCARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.firePower = x.getFirePower();
                dto.rewardGoods =  x.getRewardGoods();
                dto.numGoods = x.getNumRemovedGoods();

            }
            case FirstWarzoneCard x ->{
                dto.type = "FIRWARZONECARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.numCrewmates = x.getNumCrewmates();
                dto.directions = x.getShotsDirections();
                dto.sizes = x.getShotsSize();
            }
            case SecondWarzoneCard x ->{
                dto.type = "SECONDWARZONECARD";
                dto.idCard = x.getIdCard();
                dto.days = x.getDays();
                dto.numGoods = x.getNumGoods();
                dto.directions = x.getShotsDirections();
                dto.sizes = x.getShotsSize();
            }
            case StardustCard s->{
                dto.type = "STARDUSTCARD";
                dto.idCard = s.getIdCard();
            }
            case OpenSpaceCard o ->{
                dto.type = "OPENSPACECARD";
                dto.idCard = o.getIdCard();
            }
            case PlaugeCard p ->{
                dto.type = "PLAUGECARD";
                dto.idCard = p.getIdCard();
            }
            default -> dto.type = card.getClass().getSimpleName().toUpperCase();

        }
        return dto;
    }

    /**
     * Converts a Card object to its JSON string representation.
     * This includes transforming it to a CardDTO and then serializing it with Jackson.
     * @param card the Card to serialize
     * @return a JSON string representing the card
     * @throws JsonProcessingException if serialization fails
     */
    public String toJSON(Card card) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(card));
    }

    /**
     * Converts a list of Card objects into a JSON array string.
     * Each card is first converted into a CardDTO, then the entire list is serialized.
     * @param cards the list of Card objects to serialize
     * @return a JSON string representing the list of cards
     * @throws JsonProcessingException if serialization fails
     */
    public  String toJsonList(List<Card> cards) throws JsonProcessingException {
        List<CardDTO> dtos = new ArrayList<>();
        for (Card tmp : cards) {
            CardDTO dto = toDTO(tmp);
            dtos.add(dto);
        }
        return mapper.writeValueAsString(dtos);
    }

}

