package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

//TODO: RemoteException è pensata per errori di trasporto, tipo:
// 1) il client si è disconnesso
// 2) la rete è caduta
// 3) l’oggetto remoto non è raggiungibile
// Ma se un utente sbaglia password, o inserisce un nickname già preso, non è un errore di rete → è logica applicativa!
// Le eccezioni checked custom vengono serializzate automaticamente e propagate al client purché:
// la classe dell’eccezione sia disponibile anche nel classpath del client.
// Quindi: le eccezioni custom devono stare in un modulo condiviso, accessibile da client e server (es. shared package o jar comune).
// Quindi, domanda per te Gabriele Antonio La Vecchia, creiamo una CommunicationException e la estendiamo (ad esempio) con:
// InvalidGameIdException, UsernameAlreadyTakenException, ... (PER ME SI)
// Ovviamente, se sei d'accordo, i metodi vanno fixati.
// MIA RISPOSTA: SI, VA FATTO (GIA CREATA L'ECCEZIONE)
// RINOMINARLA IN BUSINESSLOGICEXCEPTION

//TODO: Le eccezioni di "Infrastruttura" -> wrapparle in RemoteException e lanciarle al client
//      Le eccezioni di Game Logic -> wrapparle in BusinessLogicException e lanciarle al client
// Dire agli altri di gestirle con try-catch al chiamante (?)

public class ServerRmi extends UnicastRemoteObject implements VirtualServer {
    private final GameManager gameManager;

    public ServerRmi(GameManager gameManager) throws RemoteException {
        super();
        this.gameManager = gameManager;
    }

    //TODO: gestire bene le eccezioni (i businesslogic error necessari in questi metodi inziali ?) vedere poi con l'implementazione

    @Override
    public int createNewGame (boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException, BusinessLogicException {
        try{
            return gameManager.createGame(isDemo, v, nickname, maxPlayers);
        } catch(BusinessLogicException e){
            throw new BusinessLogicException("Business-logic error during game's creation: " + e.getMessage(), e);
        } catch (IOException e){
            throw new RemoteException("IO error during game's creation: ", e);
        }
    }

    //TODO: capire se unificare il caso in cui mi unisco a una partita in cui già c'ero o sono metodi diversi
    @Override
    public void enterGame(int gameId, VirtualView v, String nickname) throws RemoteException, BusinessLogicException {
        try{
            gameManager.joinGame(gameId, v, nickname);
        } catch (BusinessLogicException e){
            throw new BusinessLogicException("Business-Logic error in joining game:  " + e.getMessage(), e);
        } catch(IOException e){
            throw new RemoteException("Error in joining game:  " + e.getMessage());
        }
    }

    @Override
    public void logOut(int gameId, String nickname) throws RemoteException, BusinessLogicException {
        try{
            gameManager.quitGame(gameId, nickname);
        } catch (BusinessLogicException e){
            throw new BusinessLogicException("Business-Logic error in joining game:  " + e.getMessage(), e);
        } catch(IOException e){
            throw new RemoteException("Error in joining game:  " + e.getMessage());
        }
    }

    //aggiustare
    @Override
    public Tile getCoveredTileServer(int gameId, String nickname) throws RemoteException {
        try{
            gameManager.getCoveredTile(gameId, nickname);
        } catch (BusinessLogicException e){
            throw new BusinessLogicException("Business-Logic error in joining game:  " + e.getMessage(), e);
        } catch(IOException e){
            throw new RemoteException("Error in joining game:  " + e.getMessage());
        }
    }


    @Override
    public String waitForResponse() throws RemoteException {
        return "";
    }

    @Override
    public void registerClient(VirtualView client) throws RemoteException {

    }

    @Override
    public int[] requestGamesList() throws RemoteException {
        return new int[0];
    }

    @Override
    public String waitForGameStart() throws Exception {
        return "";
    }

    @Override
    public Tile getUncoveredTileServer() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void rotateGlass() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void setReady() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void lookDeck() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void lookDashBoard() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void positionTile() throws RemoteException {
        throw new RemoteException("Method requires player context.");
    }

    @Override
    public void drawCard(String username) throws RemoteException, BusinessLogicException {
        try {
            gameManager.drawCard_server(username);
        } catch (IOException e) {
            throw new RemoteException("Network error while drawing card: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Business error while drawing card: " + e.getMessage(), e);
        }
    }
}

