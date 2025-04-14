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
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
        this.fire_power = fire_power;
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }

    public int getFirePower(){ return fire_power; }
}
