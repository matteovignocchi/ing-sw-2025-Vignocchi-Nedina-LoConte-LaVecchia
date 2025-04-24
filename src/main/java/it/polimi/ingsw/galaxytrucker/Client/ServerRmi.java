package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

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

//TODO: Il server riceve chiamate da un client remoto e delega al Controller la gestione della logica:
// quindi dobbiamo creare dei nuovi metodi nel controller che gestiscano la nostra logica (o almeno io penso questo)
// tutti gli errori che vedi, sono per questo motivo. Non saprei come fare diversamente

//TODO: ho inserito alcuni metodi, ma penso che dobbiamo ragionare insieme su quelli che mancano

public class ServerRmi extends UnicastRemoteObject implements VirtualServer {
    private final GameManager gameManager;

    public ServerRmi(GameManager gameManager) throws RemoteException {
        super();
        this.gameManager = gameManager;
    }

    @Override
    public int createNewGame (boolean isDemo, VirtualView v, String nickname, int maxPlayers) throws RemoteException {
        try{
            return gameManager.createGame(isDemo, v, nickname, maxPlayers);
        } catch(Exception e){
            throw new RemoteException("Error in new game's creation:  " + e.getMessage());
        }
        //capire se gestione eccezione così va bene
    }




    @Override
    public void logout(String username) throws RemoteException {

    }

    @Override
    public void enterGame(String username, int gameId) throws RemoteException {
    }

    @Override
    public void sendIndex(String username, int index) throws RemoteException {
    }

    @Override
    public void sendChoice(String username, boolean choice) throws RemoteException {
    }

    @Override
    public void sendCoordinates(String username, int x, int y) throws RemoteException {
    }

    @Override
    public void drawCard(String username) throws RemoteException {
    }

    @Override
    public void sendPlayerDash(String username, int dash) throws RemoteException {
    }

    @Override
    public void activateCard() throws RemoteException {

    }

}

