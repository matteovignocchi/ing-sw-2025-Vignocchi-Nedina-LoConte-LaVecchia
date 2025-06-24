package it.polimi.ingsw.galaxytrucker.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.galaxytrucker.DtoConvention.CardDTO;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;

import java.util.ArrayList;
import java.util.List;

public class CardSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

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
            default -> dto.type = card.getClass().getSimpleName().toUpperCase();
        }
        return dto;
    }

    public String toJSON(Card card) throws JsonProcessingException {
        return mapper.writeValueAsString(toDTO(card));
    }


    public  String toJsonList(List<Card> cards) throws JsonProcessingException {
        List<CardDTO> dtos = new ArrayList<>();
        for (Card tmp : cards) {
            CardDTO dto = toDTO(tmp);
            dtos.add(dto);
        }
        return mapper.writeValueAsString(dtos);
    }

}

