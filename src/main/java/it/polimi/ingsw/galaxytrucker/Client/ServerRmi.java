package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class ServerRmi extends UnicastRemoteObject implements VirtualServer {
    private final GameManager gameManager;

    public ServerRmi(GameManager gameManager) throws RemoteException {
        super();
        this.gameManager = gameManager;
    }

    /**
     * Metodo helper per centralizzare la gestione delle chiamate al GameManager e delle eccezioni.
     *
     * @param methodName Il nome del metodo del GameManager che viene chiamato. Usato per creare
     * messaggi di errore più informativi.
     * @param call Un'interfaccia funzionale (GameManagerCall) che incapsula la chiamata al metodo del GameManager.
     * @param <T> Il tipo di ritorno del metodo del GameManager.
     * @return Il risultato della chiamata al metodo del GameManager.
     * @throws RemoteException Se si verifica un errore di comunicazione RMI (es. problemi di rete).
     * @throws BusinessLogicException Se il GameManager lancia un'eccezione a causa di un errore nella logica di gioco.
     */
    private <T> T handleGameManagerCall(String methodName, GameManagerCall<T> call) throws RemoteException, BusinessLogicException {
        try {
            return call.execute(); // Esegue la chiamata al metodo del GameManager
        } catch (BusinessLogicException e) {
            throw new BusinessLogicException("Logic error in " + methodName + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteException("I/O error in " + methodName + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RemoteException("Unexpected error in " + methodName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Interfaccia funzionale per rappresentare una chiamata a un metodo del GameManager.  Permette
     * di passare il codice da eseguire al metodo handleGameManagerCall.
     *
     * @param <T> Il tipo di ritorno del metodo del GameManager.
     */
    private interface GameManagerCall<T> {
        T execute() throws Exception;
    }

    @Override
    public int createNewGame(boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException {
        if (v == null) { throw new RemoteException("VirtualView cannot be null"); }
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (maxPlayers < 2 || maxPlayers > 4) { throw new RemoteException("the maximum number of players must be between 2 and 4"); }

        return handleGameManagerCall("createNewGame", () -> gameManager.createGame(isDemo, v, nickname, maxPlayers));
    }

    @Override
    public void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException {
        if (v == null) { throw new RemoteException("VirtualView cannot be null"); }
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("enterGame", () -> {
            gameManager.joinGame(gameId, v, nickname);
            return null;
        });
    }

    @Override
    public void logOut(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("logOut", () -> {
            gameManager.quitGame(gameId, nickname);
            return null;
        });
    }

    @Override
    public Tile getCoveredTile(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty");}

        return handleGameManagerCall("getCoveredTile", () -> gameManager.getCoveredTile(gameId, nickname));
    }

    @Override
    public List<Tile> getUncoveredTilesList(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        return handleGameManagerCall("getUncoveredTilesList", () -> gameManager.getUncoveredTilesList(gameId, nickname));
    }

    @Override
    public Tile chooseUncoveredTile(int gameId, String nickname, int idTile) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (idTile < 0) { throw new RemoteException("Tile must be greater than 0"); }

        return handleGameManagerCall("chooseUncoveredTile", () -> gameManager.chooseUncoveredTile(gameId, nickname, idTile));
    }

    @Override
    public void dropTile(int gameId, String nickname, Tile tile) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (tile == null) { throw new RemoteException("Tile cannot be null"); }

        handleGameManagerCall("dropTile", () -> {
            gameManager.dropTile(gameId, nickname, tile);
            return null;
        });
    }

    @Override
    public void placeTile(int gameId, String nickname, Tile tile, int[] cord) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }
        if (tile == null) { throw new RemoteException("Tile cannot be null"); }
        if (cord == null || cord.length != 2 || cord[0] < 0 || cord[1] < 0) { throw new RemoteException("Invalid parameters"); }

        handleGameManagerCall("placeTile", () -> {
            gameManager.placeTile(gameId, nickname, tile, cord);
            return null;
        });
    }

    @Override
    public void setReady(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) { throw new RemoteException("Nickname cannot be null or empty"); }

        handleGameManagerCall("setReady", () -> {
            gameManager.setReady(gameId, nickname);
            return null;
        });
    }

    @Override
    public void rotateGlass(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        handleGameManagerCall("flipHourglass", () -> {
            gameManager.flipHourglass(gameId, nickname);
            return null;
        });
    }

    //valori possibili per showDeck: 0,1,2,
    @Override
    public List<Card> showDeck(int gameId, int idxDeck) throws RemoteException, BusinessLogicException {
        return handleGameManagerCall("showDeck", () -> gameManager.showDeck(gameId, idxDeck));
    }

    @Override
    public Map<Integer,int[]> requestGamesList() throws RemoteException, BusinessLogicException {
        return handleGameManagerCall("requestGamesList", gameManager::listActiveGames);
    }

    @Override
    public String waitForResponse() throws RemoteException {
        return "";
    }

    @Override
    public String waitForGameStart() throws Exception {
        return "";
    }

    @Override
    public Tile[][] lookAtDashBoard(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        if (nickname == null || nickname.trim().isEmpty()) throw new RemoteException("Nickname cannot be null or empty");

        return handleGameManagerCall("lookDashBoard", () -> gameManager.lookAtDashBoard(nickname, gameId));
    }

    @Override
    public void drawCard(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        gameManager.drawCard(gameId, nickname);
    }
}

