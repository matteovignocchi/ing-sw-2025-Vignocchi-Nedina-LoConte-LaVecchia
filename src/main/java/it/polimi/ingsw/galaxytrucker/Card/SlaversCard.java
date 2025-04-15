package it.polimi.ingsw.galaxytrucker.Card;

/**
 * "Slavers" adventure card description
 * @author Gabriele La Vecchia && Francesco Lo Conte
 */

public class SlaversCard implements Card{
    private final int days;
    private final int credits;
    private final int num_crewmates;
    private final int fire_power;

    public SlaversCard(int days, int credits, int num_crewmates, int fire_power) {
        if(days <= 0) throw new IllegalArgumentException("days must be greater than 0");
        if(credits <= 0) throw new IllegalArgumentException("credits must be greater than 0");
        if(fire_power <= 0) throw new IllegalArgumentException("fire_power must be greater than 0");
        if(num_crewmates <= 0) throw new IllegalArgumentException("num_crewmates must be greater than 0");

        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
        this.fire_power = fire_power;
    }

    @Override
    public void accept(CardVisitor visitor) throws CardEffectException{
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }

    public int getFirePower(){ return fire_power; }
}
