package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;


// Interfaccia comune per i metodi chiamabili dal client verso il server (sia RMI che Socket).
// I parametri sono progettati per l'interazione esterna: username, scelte, coordinate, ecc.


//Ho pensato questa cosa: noi gestiamo i player con un int, ma è molto più comodo che loro si registrino con un
//nickname, quindi possiamo lasciarglielo fare nel modo più comune (con una String) e, internamente, associamo
//a ciascun nickname un intero, in modo tale da seguire la logica che abbiamo pensato fin dall'inizio, ma
//permettendo comunque al player di avere un username che possa contenere qualunque carattere


public interface VirtualServer extends Remote {

    // Autenticazione e Account
    void login(String username, String password) throws RemoteException;
    void register(String username, String password) throws RemoteException;
    void logout(String username) throws RemoteException;

    // Gestione Partite
    void createNewGame(String username) throws RemoteException;
    void enterGame(String username, int gameId) throws RemoteException;

    // Azioni di gioco
    void sendIndex(String username, int index) throws RemoteException;
    void sendChoice(String username, boolean choice) throws RemoteException;
    void sendCoordinates(String username, int x, int y) throws RemoteException;
    void drawCard(String username) throws RemoteException;
    void sendPlayerDash(String username, int dash) throws RemoteException;

    // Aggiungerai altri metodi specifici man mano che le fasi evolvono.





    //METODI AGGIUNTI PER ME E MATTEO , CI SERVONO PER FAR FUNZIONARE LE COSE , SE VI RENDETE CONTO CHE MANCA QUALCOSA A VOI AGGIUNGETECELO
    //COME AVEVAMO FATTO IN CONTROLLER

    List<String> getAvaibleGames() throws RemoteException;
    Tile getTileServer() throws RemoteException;
    boolean authenticate(String username, String password) throws RemoteException;
    Void handleGameRequest(String message) throws RemoteException;
    String waitForResponse() throws RemoteException;
    void handlePlayerAction(String message) throws RemoteException;
    void registerClient(VirtualView client) throws RemoteException;
    void getUncoveredTile() throws RemoteException;
    void rotateGlass() throws RemoteException;
    void setReady()  throws RemoteException;
    void lookDeck() throws RemoteException;
    void lookDashBoard() throws RemoteException;
    void logOut() throws RemoteException;
    void activateCard() throws RemoteException;
    void getBackTile() throws RemoteException;
    void positionTile() throws RemoteException;
    void drawCard() throws RemoteException;
    void rightRotatedTile() throws RemoteException;
    void leftRotatedTile() throws RemoteException;
//    List<String> sendAvaibleGames(int id) throws RemoteException;







}

