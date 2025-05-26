package it.polimi.ingsw.galaxytrucker.Client;
import java.util.List;

public class ClientCard {
    public String type;
    public String idCard;
    public Integer days;
    public Integer credits;
    public Integer firePower;
    public Integer numCrewmates;
    public Integer numGoods;

    public List<String> stationGoods;
    public List<String> rewardGoods;
    public List<List<String>> rewardGoodsList;

    public List<Integer> directions;
    public List<Boolean> sizes;
}