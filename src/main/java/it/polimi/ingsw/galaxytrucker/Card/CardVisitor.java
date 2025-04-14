package it.polimi.ingsw.galaxytrucker.Card;

public interface CardVisitor {
    void visit(OpenSpaceCard card);
    void visit(StardustCard card);
    void visit(SmugglersCard card);
    void visit (SlaversCard card);
    void visit(SecondWarzoneCard card);
    void visit(FirstWarzoneCard card);
    // inserire gli altri visit
}
