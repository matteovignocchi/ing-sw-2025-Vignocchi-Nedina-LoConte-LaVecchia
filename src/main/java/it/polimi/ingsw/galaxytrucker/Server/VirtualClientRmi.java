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
    private int gameId;

    public VirtualClientRmi(ServerRmi server, View view) throws RemoteException {
        super();
        this.server = server;
        this.view = view;
    }
    @Override
    public void setNickname(String nickname) throws RemoteException {
     this.nickname = nickname;
    }
    @Override
    public void setView(View view) {
        this.view = view;
    }
    public void setGameId(int gameId) throws RemoteException {
        this.gameId = gameId;
    }


    /// METODI PER PRINTARE A CLIENT ///

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, int position, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        view.updateView(nickname,firePower,powerEngine,credits,position,purpleAline,brownAlien,numberOfHuman,numberOfEnergy);
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
    public void printListOfTileCovered(List<Tile> tiles) throws RemoteException {
        view.printPileCovered();
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
    public void printCard(Card card) throws RemoteException {
        view.printCard(card);
    }
    @Override
    public void printTile(Tile tile) throws RemoteException {
        view.printTile(tile);
    }
    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws RemoteException {
        this.view.printDashShip(dashboard);
    }
    @Override
    public void printDeck(List<Card> deck) throws RemoteException {
        view.printDeck(deck);
    }


    /// METODI PER CHIEDERE COSE AL CLIENT DA PARTE DAL SERVER ///

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


    /// METODI PER SETTARE COSE AL CLIENT RIGUARDO AL GAMEFLOW ///

    @Override
    public void updateGameState(GameFase fase) throws RemoteException {
        this.gameFase = fase;
        view.updateState(gameFase);
    }
    @Override
    public void startMach() throws RemoteException {

    }
    @Override
    public GameFase getCurrentGameState() throws RemoteException {
        return gameFase;
    }
    @Override
    public GameFase getGameFase() {
        return gameFase;
    }


    /// METODI PER IL LOGIN ///

    @Override
    public boolean sendLogin(String username) throws RemoteException {
//        return server.authenticate(username);
        return true;
    }
    @Override
    public int sendGameRequest(String message) throws RemoteException {
        if(message.contains("create")){
            boolean tmp = view.ask("would you like a demo version?");
            int tmpInt= 5;
            do{
                view.inform("select max 4 players");
                tmpInt = view.askIndex();
            }while(tmpInt>4);
            try {
                 return server.createNewGame(tmp , this , nickname ,tmpInt );
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
                 return availableGames[choice-1];
            } catch (BusinessLogicException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
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


    /// COMANDI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public Tile getTileServer() throws Exception {
        return server.getCoveredTile(gameId, nickname);
    }
    @Override
    public Tile getUncoveredTile() throws Exception{
        List<Tile> tmp = server.getUncoveredTilesList(gameId, nickname);
        view.printPileShown(tmp);
        int index = askIndex();
        Tile tmpTile =tmp.get(index);
            return server.chooseUncoveredTile(gameId, nickname,tmpTile.getIdTile());
    }
    @Override
    public void getBackTile(Tile tile) throws Exception{
        server.dropTile(gameId,nickname,tile);
    }
    @Override
    public void positionTile(Tile tile) throws Exception{
        view.inform("choose coordinate");
        int[] tmp = view.askCordinate();
        server.placeTile(gameId, nickname, tile, tmp);
    }
    @Override
    public void drawCard() throws Exception {
        server.drawCard();
    }
    @Override
    public void rotateGlass() throws Exception{
        server.rotateGlass(gameId,nickname);
    }
    @Override
    public void setReady() throws Exception{
        server.setReady(gameId,nickname);
    }
    @Override
    public void lookDeck() throws Exception{
        view.inform("choose deck : 1 / 2 / 3");
        int index = askIndex();
        server.showDeck(gameId, index);
    }
    @Override
    public void lookDashBoard() throws Exception{
        server.lookDashBoard(gameId,nickname);
    }
    @Override
    public void logOut() throws Exception{
        server.logOut(gameId,nickname);
    }

}
