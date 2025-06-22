//package it.polimi.ingsw.galaxytrucker.Client;
//
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//class DummyViewTest {
//
//    @Test
//    void testSendAvailableChoices() {
//        DummyView view = new DummyView(false); // Crea una vista in modalità non demo
//        String choice = view.sendAvailableChoices();
//        assertEquals("get a covered tile", choice); // Verifica che la scelta sia quella corretta
//    }
//
//    @Test
//    void testPrintListOfCommands() {
//        DummyView view = new DummyView(false);
//        // Cattura l'output su console per verificare che le opzioni siano stampate correttamente
//        // In un vero test, si può usare un PrintStream personalizzato per catturare l'output.
//        view.printListOfCommand();
//        view.start();
//        // Verifica che le opzioni siano stampate correttamente (verifica visuale per ora)
//    }
//
//    @Test
//    void testAskIndex() {
//        DummyView view = new DummyView(false);
//        int index = view.askIndex();
//        assertEquals(1, index); // Verifica che l'input simulato sia 1
//    }
//}
