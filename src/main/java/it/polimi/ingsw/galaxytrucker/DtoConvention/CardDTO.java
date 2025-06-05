package it.polimi.ingsw.galaxytrucker.DtoConvention;

import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.util.List;

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