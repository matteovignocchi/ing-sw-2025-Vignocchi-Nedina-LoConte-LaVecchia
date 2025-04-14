package it.polimi.ingsw.galaxytrucker.Card;

public interface CardVisitor {
    void visit(OpenSpaceCard card);
    void visit(StardustCard card);
    void visit(SmugglersCard card);
    void visit(SlaversCard card);
    void visit(SecondWarzoneCard card);
    void visit(FirstWarzoneCard card);
    void visit(AbandonedShipCard card);
    void visit(AbandonedStationCard card);
    void visit(MeteoritesRainCard card);
    void visit(PiratesCard card);
    void visit(PlanetsCard card);
    void visit(PlaugeCard card);
}
