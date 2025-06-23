package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {
    private Controller controller;
    private TestView viewA;
    private TestView viewB;
    private List<Integer> endedGames;

    static class TestView implements VirtualView {
        List<String> states = new ArrayList<>();
        List<Map<String,int[]>> maps = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        String lastTileJson;
        boolean demoFlag;

        @Override public void updateGameState(String phase) { states.add(phase); }
        @Override public void updateMapPosition(Map<String,int[]> position) { maps.add(position); }

        @Override
        public void setStart() throws Exception {

        }

        @Override
        public String askInformationAboutStart() throws Exception {
            return "";
        }

        @Override public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAlien, boolean brownAlien, int numberOfHuman, int numberOfEnergy) {}
        @Override public void setTile(String jsonTmp) { lastTileJson = jsonTmp; }
        @Override public void inform(String message) { messages.add(message); }
        @Override public void reportError(String error) {}
        @Override public boolean askWithTimeout(String question) { return false; }
        @Override public int[] askCoordsWithTimeout() { return null; }
        @Override public Integer askIndexWithTimeout() { return null; }
        @Override public void printPlayerDashboard(String[][] dash) {}
        @Override public void printListOfTileCovered(String jsonTiles) {}
        @Override public void printListOfTileShown(String jsonTiles) {}
        @Override public void printListOfGoods(List<String> list) {}
        @Override public void printCard(String jsonCard) {}
        @Override public void printTile(String jsonTile) {}
        @Override public void printDeck(String jsonDeck) {}
        @Override public Boolean ask(String message) { return null; }
        @Override public Integer askIndex() { return null; }
        @Override public int[] askCoordinate() { return null; }
        @Override public String askString() { return null; }
        @Override public void setIsDemo(Boolean isDemo) { demoFlag = isDemo; }
        @Override public void enterGame(int gameId) throws Exception {}
        @Override public void leaveGame() throws Exception {}
        @Override public String takeReservedTile() throws Exception {return "";}
        @Override public void setClientController(it.polimi.ingsw.galaxytrucker.Client.ClientController controller) {}
        @Override public void setGameId(int gameId) {}
        @Override public void startMach() {}
        @Override public int sendLogin(String username) throws Exception {return 0;}
        @Override public int sendGameRequest(String message, int numberOfPlayer, Boolean isdemo) throws Exception {return 0;}
        @Override public String getTileServer() throws Exception {return "";}
        @Override public String getUncoveredTile() throws Exception {return "";}
        @Override public void getBackTile(String tile) throws Exception {}
        @Override public void positionTile(String tile) throws Exception {}
        @Override public void drawCard() throws Exception {}
        @Override public void rotateGlass() throws Exception {}
        @Override public void setReady() throws Exception {}
        @Override public void lookDeck() throws Exception {}
        @Override public void lookDashBoard() throws Exception {}
        @Override public void logOut() throws Exception {}
        @Override public void setNickname(String nickname) throws Exception {}
        @Override public void updateDashMatrix(String[][] data) {}
    }

    @BeforeEach
    public void setup() throws Exception {
        endedGames = new ArrayList<>();
        controller = new Controller(true, 99, 2, endedGames::add, Collections.synchronizedSet(new HashSet<>()));
        viewA = new TestView();
        viewB = new TestView();
    }

    @Test
    public void testAddPlayerSuccess() throws Exception {
        controller.addPlayer("Alice", viewA);
        assertEquals(1, controller.getPlayersByNickname().size());
        controller.addPlayer("Bob", viewB);
        assertEquals(2, controller.getPlayersByNickname().size());

        // Verify viewA got initial calls
        assertTrue(viewA.demoFlag);
        assertFalse(viewA.states.isEmpty());
        assertNotNull(viewA.lastTileJson);
        assertTrue(viewA.messages.stream().anyMatch(m -> m.contains("Alice  joined")));
    }

    @Test
    public void testAddDuplicatePlayerThrows() throws Exception {
        controller.addPlayer("Alice", viewA);
        assertThrows(BusinessLogicException.class,
                () -> controller.addPlayer("Alice", viewB));
    }

    @Test
    public void testAddPlayerGameFullThrows() throws Exception {
        controller.addPlayer("Alice", viewA);
        controller.addPlayer("Bob", viewB);
        TestView viewC = new TestView();
        assertThrows(BusinessLogicException.class,
                () -> controller.addPlayer("Charlie", viewC));
    }

    @Test
    public void testGetPlayerCheckAndNicknameMapping() throws Exception {
        controller.addPlayer("Alice", viewA);
        Player p = controller.getPlayerCheck("Alice");
        assertNotNull(p);
        assertEquals("Alice", controller.getNickByPlayer(p));
        assertThrows(BusinessLogicException.class,
                () -> controller.getPlayerCheck("Unknown"));
    }

    @Test
    public void testCountAndDisconnectReconnect() throws Exception {
        controller.addPlayer("Alice", viewA);
        controller.addPlayer("Bob", viewB);
        assertEquals(2, controller.countConnectedPlayers());

        controller.markDisconnected("Alice");
        assertEquals(1, controller.countConnectedPlayers());
        assertTrue(viewB.messages.stream().anyMatch(m -> m.contains("Alice is disconnected")));

        // Reconnect
        TestView viewA2 = new TestView();
        controller.markReconnected("Alice", viewA2);
        assertEquals(2, controller.countConnectedPlayers());
        assertTrue(viewA2.messages.stream().anyMatch(m -> m.contains("Alice is reconnected")));
    }

    @Test
    public void testIsGameStarted() throws Exception {
        controller.addPlayer("Alice", viewA);
        assertTrue(controller.isGameStarted(), "Game should be started when phase != WAITING_FOR_PLAYERS");
    }

    @Test
    public void testGetPlayersPosition() throws Exception {
        controller.addPlayer("Alice", viewA);
        Map<String,int[]> pos = controller.getPlayersPosition();
        assertTrue(pos.containsKey("Alice"));
        int[] arr = pos.get("Alice");
        assertEquals(3, arr.length);
        // elimination flag should be 0
        assertEquals(0, arr[2]);
    }
}
