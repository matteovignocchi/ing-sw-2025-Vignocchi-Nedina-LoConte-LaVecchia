package it.polimi.ingsw.galaxytrucker.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Card interface
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
    void accept (CardVisitor visitor) throws CardEffectException;
}
