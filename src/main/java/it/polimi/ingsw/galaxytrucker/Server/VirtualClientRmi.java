package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.Client.ServerRmi;
import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.View.View;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    private final ServerRmi server;
    private View view;
    private GameFase gameFase;
    private String nickname;

    public VirtualClientRmi(ServerRmi server, View view) throws RemoteException {
        super();
        this.server = server;
        this.view = view;
    }
    public void setNickname(String nickname) throws RemoteException {
     this.nickname = nickname;
    }

    //PARTE VIEW
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void showUpdate() throws RemoteException {
        view.updateState(gameFase);
    }

    @Override
    public void inform(String message) throws RemoteException {
        view.inform(message);
    }


    @Override
    public void reportError(String error) throws RemoteException {
        view.reportError(error);
    }

    @Override
    public boolean ask(String message) throws RemoteException {
        return view.ask(message);
    }

    @Override
    public int askIndex() throws RemoteException {
        return view.askIndex();
    }

    @Override
    public int[] askCoordinate() throws RemoteException {
        return view.askCordinate();

    }

    @Override
    public String askString() throws RemoteException {
        return view.askString();
    }

    @Override
    public void printCard(Card card) throws RemoteException {
        view.printCard(card);
    }

    @Override
    public void printListOfTileCovered(List<Tile> tiles) throws RemoteException {
        view.printPileCovered(tiles);
    }

    @Override
    public void printListOfTileShown(List<Tile> tiles) throws RemoteException {
        view.printPileShown(tiles);
    }

    @Override
    public void printListOfGoods(List<Colour> listOfGoods) {
        view.printListOfGoods(listOfGoods);
    }

    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws RemoteException {
        this.view.printDashShip(dashboard);
    }


    //FASI DI GIOCO
    @Override
    public void updateGameState(GameFase fase) throws RemoteException{
        this.gameFase = fase;
        view.updateState(gameFase);
        showUpdate();
    }



    //PARTI DI GIOCO
    @Override
    public void startMach() throws RemoteException {

    }

//    @Override
//    public List<String> requestGameList() throws RemoteException {
//         return server.getAvaibleGames();
//    }


    //tutti questi metodi get non sono inutili dato che è sempre una
    //lista di comandi, tanto io mando il comando e il server me la printa

    @Override
    public GameFase getCurrentGameState() throws RemoteException {
        return gameFase;
    }

    @Override
    public boolean sendLogin(String username, String password) throws RemoteException {
        return server.authenticate(username, password);
    }

    @Override
    public boolean sendGameRequest(String message) throws RemoteException {
        if(message.contains("create")){
            boolean tmp = view.ask("would you like a demo version?");
            int tmpInt= 5;
            do{
                view.inform("select max 4 players");
                tmpInt = view.askIndex();
            }while(tmpInt>4);
            try {
                server.createNewGame(tmp , this , nickname ,tmpInt );
            } catch (BusinessLogicException e) {
                throw new RuntimeException(e);
            }
        }
     if(message.contains("login")){
         view.inform("Available Games");
         int[] availableGames = server.requestGamesList();
         for(int i = 0 ; i < availableGames.length; i++){
             view.inform((i+1) + "." + availableGames[i]);
         }
         int choice = askIndex();
         try {
             server.enterGame(availableGames[choice-1], this , nickname);
         } catch (BusinessLogicException e) {
             throw new RuntimeException(e);
         }
     }
     return true;
    }

    @Override
    public Object waitForResponce() throws RemoteException {
        return server.waitForResponse();
    }

    @Override
    public String waitForGameUpadate() throws RemoteException {
        try {
            return server.waitForGameStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameFase getGameFase() {
        return gameFase;
    }

    public Tile getTileServer() throws Exception {
        return server.getCoveredTileServer();
    }
    public Tile getUncoveredTile() throws Exception{
        return server.getUncoveredTileServer();
    }
    public void getBackTile(Tile tile) throws Exception{
        server.getBackTile();
    }
    public void positionTile(Tile tile) throws Exception{
        server.positionTile();
    }
    public void drawCard() throws Exception {
        server.drawCard();
    }
    public void rotateGlass() throws Exception{
        server.rotateGlass();
    }
    public void setReady() throws Exception{
        server.setReady();
    }
    public void lookDeck() throws Exception{
        server.lookDeck();
    }
    public void lookDashBoard() throws Exception{
        server.lookDashBoard();
    }
    public void logOut() throws Exception{
        server.logOut();
    }

}
