package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;

import java.util.List;

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
    void placeTile(Tile tile) throws Exception;
    Tile[][] sendPlayerDash(int i) throws Exception;
    //metodo per il login  , lo chiama il client quando si connette e chiama gli altri metodi per fare cosine
    void registerClient(VirtualView client) throws Exception;

    //ci saranmno i vari metodi di send delle informazioni
    //chiamo metodo virtual server -> il metodo in virtual server chiama il metodo in view per restituire le robe

    //che poi verrranno implementati in modo diverso in base al tipo di comunicazione


    //IN CONTROLLER VA FATTO IN MODO CHE INVECE CHE CHIAMARE I METODI SU PLAYERVIEW LI CHIAMI DIRETTAMENTI SUL PLAYER TRAMITE SERVER , NON AVRò PIù
    //LA LISTA DI VIEW MA DIRETTAMENTE CHIAMATE A METODI PUBBLICI DEI CLIENT


    /// ///////////////////
    //metodi aggiunti in post
    boolean authenticate(String username, String password) throws Exception;
    List<String> getAvaibleGames() throws Exception;
    String handleGameRequest(String request) throws Exception;
    String handlePlayerAction(String action) throws Exception;
    String waitForResponnse() throws Exception;



}
