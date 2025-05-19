package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    //private final ServerRmi server;
    private final VirtualServer server;
    private View view;
    private GamePhase gamePhase;
    private String nickname;
    private int gameId = 0;
    private Tile[][] Dash_Matrix;
    private final Object startLock = new Object();
    private String start = "false";

    public VirtualClientRmi(VirtualServer server, View view) throws RemoteException {
        super();
        this.server = server;
        this.view = view;
        Dash_Matrix = new Tile[5][7];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                Dash_Matrix[i][j] = new EmptySpace();
            }
        }
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
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        view.updateView(nickname,firePower,powerEngine,credits,purpleAline,brownAlien,numberOfHuman,numberOfEnergy);
    }

    public void setCentralTile(Tile tmp){
        Dash_Matrix[2][3] = tmp;
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
        return view.askCoordinate();

    }
    @Override
    public String askString() throws RemoteException {
        return view.askString();
    }


    /// METODI PER SETTARE COSE AL CLIENT RIGUARDO AL GAMEFLOW ///

    @Override
    public void updateGameState(GamePhase fase) throws RemoteException {
        this.gamePhase = fase;
        view.updateState(gamePhase);
    }
    @Override
    public void startMach() throws RemoteException {

    }
    @Override
    public GamePhase getCurrentGameState() throws RemoteException {
        return gamePhase;
    }
    @Override
    public GamePhase getGameFase() {
        return gamePhase;
    }


    /// METODI PER IL LOGIN ///

    @Override
    public int sendLogin(String username) throws RemoteException {

        try {
            return server.logIn(username , this);
        } catch (BusinessLogicException e) {
            return -1;
        }

    }
    @Override
    public int sendGameRequest(String message) throws RemoteException {
        if(message.contains("CREATE")){
            while (true){
                switch (view){
                    case TUIView v ->{
                        boolean demo = v.ask("would you like a demo version?");
                        v.inform("select max 4 players");
                        int numberOfPlayer = v.askIndex()+1;
                        try {
                            return server.createNewGame(demo , this , nickname , numberOfPlayer);
                        } catch (Exception e) {
                            v.reportError("you miss +" + e.getMessage() );
                        }
                    }
                    case GUIView v -> {
                        try {
                            List<Object> data = v.getDataForGame();
                            boolean demo = (boolean) data.get(0);
                            int numberOfPlayer = (int) data.get(1);
                            return server.createNewGame(demo, this, nickname, numberOfPlayer);
                        } catch (Exception e) {
                            v.reportError("you miss +" + e.getMessage());
                        }
                    }
                    default -> {}
                }
            }

        }
        if(message.contains("JOIN")){
            while (true){
                view.inform("Available Games");
                Map<Integer, int[]> availableGames = Map.of();
                try {
                    availableGames = server.requestGamesList();
                } catch (BusinessLogicException e) {
                    view.reportError("you miss " + e.getMessage() );
                }
                if(availableGames.isEmpty()){
                    view.inform("No available games");
                    return -1;
                } else {
                    for (Integer i : availableGames.keySet()) {
                        int[] info = availableGames.get(i);
                        if(info[2] == 1){
                            view.inform(i + ". Players in game : " + info[0] + "/" + info[1] + " DEMO");
                        }else{
                            view.inform(i + ". Players in game : " + info[0] + "/" + info[1]);
                        }
                    }
                }
                int choice;
               while(true){
                    choice = askIndex()+1;
                    if(availableGames.containsKey(choice)) break;
                   view.inform("index not valid");
               }
                try {
                    server.enterGame(choice, this , nickname);
                    return choice;
                } catch (Exception e) {
                    view.reportError("you miss " + e.getMessage() );
                }
            }

        }
        return 0;
    }

    /// COMANDI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public Tile getTileServer() throws RemoteException , BusinessLogicException {
            try {
                Tile tmp = server.getCoveredTile(gameId, nickname);
                return tmp;
            } catch (BusinessLogicException e) {
                throw new RemoteException(e.getMessage());
            }
        }

    @Override
    public Tile getUncoveredTile() throws BusinessLogicException ,  RemoteException {
        List<Tile> tmp;
        try {
            tmp = server.getUncoveredTilesList(gameId, nickname);

        } catch (Exception e) {
            throw new BusinessLogicException("Empty list");
        }

        view.printPileShown(tmp);
        view.inform("selected tile");
        int index;
        while(true){
            while (true) {
                index = askIndex();
                if (index >= 0 && index < tmp.size()) break;
                view.inform("Invalid index. Try again.");
            }
            try {
                return server.chooseUncoveredTile(gameId, nickname,tmp.get(index).getIdTile());
            } catch (BusinessLogicException e) {
                view.reportError("you miss " + e.getMessage() + "select new index" );
            }
        }
    }
    @Override
    public void getBackTile(Tile tile) throws RemoteException , BusinessLogicException{
            try {
                server.dropTile(gameId,nickname,tile);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
        }

    @Override
    public void positionTile(Tile tile) throws RemoteException{
        view.printDashShip(Dash_Matrix);
        int[] tmp;
        while(true){
            view.inform("Choose coordinates");
            tmp = view.askCoordinate();
            try {
                server.placeTile(gameId, nickname, tile, tmp);
                break;
            } catch (BusinessLogicException e) {
                view.reportError(e.getMessage());
            }
        }
        Dash_Matrix[tmp[0]][tmp[1]] = tile;
        view.printDashShip(Dash_Matrix);
    }
    @Override
    public void drawCard() throws RemoteException , BusinessLogicException {
            try {
                server.drawCard(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());            }
        }


    @Override
    public void rotateGlass() throws RemoteException ,  BusinessLogicException {
            try {
                server.rotateGlass(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
    }


    @Override
    public void setReady() throws RemoteException , BusinessLogicException{
            try {
                server.setReady(gameId,nickname);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
        }

    @Override
    public void lookDeck() throws RemoteException{
        view.inform("choose deck : 1 / 2 / 3");
        int index;
        while(true){
            index = askIndex();
            try {
                List<Card> deck = server.showDeck(gameId, index);
                view.printDeck(deck);
                break;
            } catch (BusinessLogicException e) {
                view.reportError("index not valid");
            } catch (IOException e) {
                view.reportError("insert a number");
            }
        }
    }

    @Override
    public void lookDashBoard() throws RemoteException{
        Tile[][] dashPlayer;
        String tmp;
        while(true){
             tmp = view.choosePlayer();
            try {
                dashPlayer = server.lookAtDashBoard(gameId,tmp);
                break;
            } catch (Exception e) {
                view.reportError("player not valid");
            }
        }
        view.inform("Space Ship of :" + tmp);
        view.printDashShip(dashPlayer);
        view.printListOfCommand();
    }
    @Override
    public void logOut() throws RemoteException{
        if(gameId != 0) {
            try {
                server.LeaveGame(gameId, nickname);
            } catch (Exception e) {
                view.reportError("problem with server");
            }
        }else{
                try {
                    server.logOut(nickname);
                } catch (Exception  e) {
                    view.reportError("problem with server");
                }
        }
    }

    @Override
    public void updateMapPosition(Map<String, Integer> Position) throws RemoteException {
        view.updateMap(Position);
    }

    @Override
    public void setStart() throws RemoteException{
        synchronized (startLock) {
            start = "start";
        }    }

    @Override
    public String askInformationAboutStart() throws RemoteException {
        synchronized (startLock) {
            return start;
        }
    }

    @Override
    public void setIsDemo(Boolean demo) throws RemoteException{
        view.setIsDemo(demo);
    }

    //////////////////////////////////////////////////
    //TODO:solo una bozza, scriverlo meglio. Stesso per socket
    public void enterGame(int gameId) throws RemoteException {
        try{
            server.enterGame(gameId, this , nickname);
            setGameId(gameId);
            //TODO: continuare come bisogna fare
        } catch (Exception e) {
            view.reportError("you miss " + e.getMessage() );
        }
    }

}
