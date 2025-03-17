package it.polimi.ingsw.galaxytrucker.Card;

import it.polimi.ingsw.galaxytrucker.FlightCardBoard;
import it.polimi.ingsw.galaxytrucker.Player;
import java.util.ArrayList;
import java.util.List;

// 1. Aggiungere eccezioni nei costruttori ?
// 2. I meteoriti colpiscono in contemporanea.. gestire con thread ???

public class MeteoritesRainCard implements Card{
    private final List<Integer> directions;
    private final List<Boolean> size;

    public MeteoritesRainCard(List<Integer> directions, List<Boolean> size) throws IllegalArgumentException{
        if(directions.size() != size.size()) throw new IllegalArgumentException("Different lists' dimensions");
        this.directions = new ArrayList<>(directions);
        this.size = new ArrayList<>(size);
    }

    @Override
    public void activate(List<Player> players, FlightCardBoard f) throws IllegalArgumentException{
        if(players == null) throw new IllegalArgumentException("Null players list");
        else if(players.isEmpty()) throw new IllegalArgumentException("Empty players list");
        else if(f == null) throw new IllegalArgumentException("Null flight card board");


    }
}
