package it.polimi.ingsw.galaxytrucker.Model.Card;

import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;

/**
 * This interface follows the Visitor design pattern, allowing new operations to be performed on
 * Card objects without changing their classes. Each concrete card type implements the "accept" method,
 * which invokes the appropriate "visit" method defined here. Implementations of this interface define
 * the behavior that should occur when a specific card is activated during gameplay.
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */
public interface CardVisitor {
    /**
     * Apply OpenSpaceCard effect
     * @param card: card.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(OpenSpaceCard card) throws BusinessLogicException;
    /**
     * Apply StardustCard effect
     * @param card: card.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(StardustCard card) throws CardEffectException, BusinessLogicException;
    /**
     * Apply SmugglersCard effect
     * @param card: card.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(SmugglersCard card) throws BusinessLogicException;
    /**
     * Apply SlaversCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(SlaversCard card) throws BusinessLogicException;
    /**
     * Apply SecondWarzoneCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(SecondWarzoneCard card) throws BusinessLogicException;
    /**
     * Apply FirstWarzoneCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(FirstWarzoneCard card) throws BusinessLogicException;
    /**
     * Apply AbandonedShipCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(AbandonedShipCard card) throws BusinessLogicException;
    /**
     * Apply AbandonedStationCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(AbandonedStationCard card) throws BusinessLogicException;
    /**
     * Apply MeteoritesRainCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(MeteoritesRainCard card) throws BusinessLogicException;
    /**
     * Apply PiratesCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(PiratesCard card) throws BusinessLogicException;
    /**
     * Apply PlanetsCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(PlanetsCard card) throws BusinessLogicException;
    /**
     * Apply PlagueCard effect
     * @param card: card object on which the method is activated.
     * @throws CardEffectException: custom exception that is thrown in case of problems activating the card.
     */
    void visit(PlagueCard card) throws BusinessLogicException;
}