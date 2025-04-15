package it.polimi.ingsw.galaxytrucker.Server.Model.Card;

public interface CardVisitor {
    void visit(OpenSpaceCard card) throws CardEffectException;
    void visit(StardustCard card) throws CardEffectException;
    void visit(SmugglersCard card) throws CardEffectException;
    void visit(SlaversCard card) throws CardEffectException;
    void visit(SecondWarzoneCard card) throws CardEffectException;
    void visit(FirstWarzoneCard card) throws CardEffectException;
    void visit(AbandonedShipCard card) throws CardEffectException;
    void visit(AbandonedStationCard card) throws CardEffectException;
    void visit(MeteoritesRainCard card) throws CardEffectException;
    void visit(PiratesCard card) throws CardEffectException;
    void visit(PlanetsCard card) throws CardEffectException;
    void visit(PlaugeCard card) throws CardEffectException;
}
