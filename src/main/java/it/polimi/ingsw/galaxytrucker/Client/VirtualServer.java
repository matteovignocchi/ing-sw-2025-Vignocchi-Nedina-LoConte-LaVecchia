package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer {
    void sendIndex(int index) throws Exception;
    void sendChoice(boolean choice) throws Exception;
    void sendCoordinates(int x, int y) throws Exception;
    void sendLogin(String username) throws Exception;
    void login(String username, String password) throws Exception;
    void createNewAccount() throws Exception;
    void createNewGame() throws Exception;
    void enterGame(int id) throws Exception;
    void logout() throws Exception;
    void drawCard() throws Exception;
    void sendPlayerDash(int i) throws Exception;
    //ci saranmno i vari metodi di send delle informazioni
    //chiamo metodo virtual server -> il metodo in virtual server chiama il metodo in view per restituire le robe

    //che poi verrranno implementati in modo diverso in base al tipo di comunicazione



}
