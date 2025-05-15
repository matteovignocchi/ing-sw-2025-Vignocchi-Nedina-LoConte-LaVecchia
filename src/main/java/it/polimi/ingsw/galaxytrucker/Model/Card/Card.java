package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;

/**
 * Card interface that represents a generic adventure card in the game.
 * It also supports polymorphic deserialization from JSON using Jackson.
 * The "JsonTypeInfo" and "JsonSubTypes" annotations enable the ObjectMapper
 * to correctly instantiate the specific card subclass based on the "type" property in the JSON file.
 * @author Gabriele La vecchia && Francesco Lo Conte
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OpenSpaceCard.class, name = "OpenSpaceCard"),
        @JsonSubTypes.Type(value = PiratesCard.class, name = "PiratesCard"),
        @JsonSubTypes.Type(value = PlaugeCard.class, name = "PlaugeCard"),
        @JsonSubTypes.Type(value = PlanetsCard.class, name = "PlanetsCard"),
        @JsonSubTypes.Type(value = AbandonedShipCard.class, name = "AbandonedShipCard"),
        @JsonSubTypes.Type(value = FirstWarzoneCard.class, name = "FirstWarzoneCard"),
        @JsonSubTypes.Type(value = MeteoritesRainCard.class, name = "MeteoritesRainCard"),
        @JsonSubTypes.Type(value = SecondWarzoneCard.class, name = "SecondWarzoneCard"),
        @JsonSubTypes.Type(value = SlaversCard.class, name = "SlaversCard"),
        @JsonSubTypes.Type(value = SmugglersCard.class, name = "SmugglersCard"),
        @JsonSubTypes.Type(value = StardustCard.class, name = "StardustCard"),
        @JsonSubTypes.Type(value = AbandonedStationCard.class, name = "AbandonedStationCard")
})
public interface Card {

    /**
     *This method allows the Visitor pattern to be used by allowing an external "CardVisitor" to perform operations
     * based on the specific card type. Each card implementation will override this method
     * by passing itself as an argument.
     * @param visitor:  the visitor that will handle the card effect.
     * @throws CardEffectException: custom exception that handles an error while executing the card effect.
     */

    void accept (CardVisitor visitor) throws BusinessLogicException;
}