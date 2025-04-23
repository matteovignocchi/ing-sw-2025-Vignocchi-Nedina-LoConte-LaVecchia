package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Controller.Controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

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

    private final Controller controller;
    private final Map<String, Integer> userToId;
    private int id_player;
    //la uso per fare la traduzione che ho pensato (vedi commento in VirtualServer)

    public ServerRmi(Controller controller) throws RemoteException {
        this.controller = controller;
        this.userToId = new HashMap<>();
        this.id_player = 0;
    }

    @Override
    public void login(String username, String password) throws RemoteException {
        if (userToId.containsKey(username))
            throw new RemoteException("Utente già connesso");

        int id = id_player;
        id_player++;
        userToId.put(username, id);
        System.out.println("Login effettuato: " + username + " -> id " + id);
    }

    @Override
    public void logout(String username) throws RemoteException {

    }

    @Override
    public void createNewGame(String username) throws RemoteException {
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

}

