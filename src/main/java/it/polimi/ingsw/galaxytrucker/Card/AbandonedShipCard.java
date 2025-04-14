package it.polimi.ingsw.galaxytrucker.Card;
/**
 * This class describes the behavior of the "Abandoned Ship" adventure card.
 * @author Francesco Lo Conte && Gabriele La Vecchia
 */

public class AbandonedShipCard implements Card{

    private final int days;
    private final int credits;
    private final int num_crewmates;

    public AbandonedShipCard(int days, int credits, int num_crewmates) {
        this.days = days;
        this.credits = credits;
        this.num_crewmates = num_crewmates;
    }

    @Override
    public void accept(CardVisitor visitor){
        visitor.visit(this);
    }

    public int getDays(){ return days; }

    public int getCredits(){ return credits; }

    public int getNumCrewmates(){ return num_crewmates; }
}
