package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Model.Card.FirstWarzoneCard;
import it.polimi.ingsw.galaxytrucker.Model.Card.MeteoritesRainCard;
import it.polimi.ingsw.galaxytrucker.Model.Card.PiratesCard;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import java.util.List;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import org.junit.jupiter.api.Test;
import it.polimi.ingsw.galaxytrucker.Model.Card.*;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import org.junit.jupiter.api.Test;

import java.util.List;

class TUIViewPrintAllCardsTest {

    @Test
    void printAllCards() throws Exception {
        TUIView tui = new TUIView();
        tui.printCard(new AbandonedShipCard("asc1", 1, 3, 5));
        System.out.println("\n");
        tui.printCard(new AbandonedStationCard("ast1", 2, 4, List.of(Colour.RED, Colour.BLUE)));
        System.out.println("\n");
        tui.printCard(new FirstWarzoneCard(
                "fwz1",
                3,
                2,
                List.of(0, 1, 2),
                List.of(true, false, true)
        ));
        System.out.println("\n");
        tui.printCard(new SecondWarzoneCard(
                "swz1",
                2,
                3,
                List.of(3, 0),
                List.of(false, true)
        ));
        System.out.println("\n");
        tui.printCard(new MeteoritesRainCard(
                "mrc1",
                List.of(1, 2, 3),
                List.of(true, false, true)
        ));
        System.out.println("\n");
        tui.printCard(new OpenSpaceCard("osc1"));
        System.out.println("\n");
        tui.printCard(new StardustCard("stc1"));
        System.out.println("\n");
        tui.printCard(new PiratesCard(
                "pc1",
                5,                  // fire_power
                1,                  // days
                7,                  // credits
                List.of(0, 3),      // shots_directions
                List.of(true, false)
        ));
        System.out.println("\n");
        tui.printCard(new PlanetsCard(
                "plc1",
                List.of(
                        List.of(Colour.RED, Colour.RED),
                        List.of(Colour.BLUE),
                        List.of(Colour.GREEN, Colour.YELLOW)
                ),
                2  // days
        ));
        System.out.println("\n");
        tui.printCard(new PlaugeCard("puc1"));
        System.out.println("\n");
        tui.printCard(new SlaversCard(
                "slc1",
                4,  // fire_power
                3,  // numCrewmates
                6,  // credits
                2   // days
        ));
        System.out.println("\n");
        tui.printCard(new SmugglersCard(
                "smc1",
                1,  // days
                6,  // fire_power
                2,  // num_removed_goods
                List.of(Colour.YELLOW, Colour.BLUE)
        ));
    }
}

