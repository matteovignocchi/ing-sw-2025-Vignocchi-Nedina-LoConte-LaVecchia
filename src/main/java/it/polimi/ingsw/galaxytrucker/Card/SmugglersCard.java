package it.polimi.ingsw.galaxytrucker.Card;
import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import it.polimi.ingsw.galaxytrucker.Token.Good;
import java.util.ArrayList;
import java.util.List;

// DA COMMENTARE

public class SmugglersCard implements Card{
    private final int days;
    private final int fire_power;
    private final int num_removed_goods;
    private final List<Good> reward_goods;

    public SmugglersCard(int days, int fire_power, int r_goods, List<Good> reward_goods) {
        this.days = days;
        this.fire_power = fire_power;
        this.num_removed_goods = r_goods;
        this.reward_goods = new ArrayList<>(r_goods);
    }

    @Override
    public void activate (List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");

        for (Player p : players){
            if(p.getFirePower() > fire_power){
                if(p.askSmugglers(days, reward_goods)){ //Capire se modificare
                    f.moveRocket(-days, p, players);
                    p.addGoods(reward_goods);
                }
                break;
            } else if (p.getFirePower() < fire_power){
                p.removeGoods(num_removed_goods);
            }
        }
    }
}
