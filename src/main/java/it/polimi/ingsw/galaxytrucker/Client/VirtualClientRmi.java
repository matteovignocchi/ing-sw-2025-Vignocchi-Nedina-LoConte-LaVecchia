package it.polimi.ingsw.galaxytrucker.Client;

import it.polimi.ingsw.galaxytrucker.BusinessLogicException;
import it.polimi.ingsw.galaxytrucker.GamePhase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Colour;
import it.polimi.ingsw.galaxytrucker.Model.Tile.EmptySpace;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class VirtualClientRmi extends UnicastRemoteObject implements VirtualView {
    private String nickname;
    private int gameId = 0;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private transient ClientController ciccio;
    private final VirtualServer server;
    private boolean printedWaiting = false;
    private boolean printedStarting = false;

    public VirtualClientRmi(VirtualServer server) throws RemoteException {
        super();
        this.server = server;
    }
    @Override
    public void setNickname(String nickname) throws RemoteException {
        this.nickname = nickname;
    }

    public void setGameId(int gameId) throws RemoteException {
        this.gameId = gameId;
    }
    @Override
    public void setClientController(ClientController clientController) {
        this.ciccio = clientController;
    }


    /// METODI PER PRINTARE A CLIENT ///

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        ciccio.showUpdateByController(nickname,firePower,powerEngine,credits,purpleAline,brownAlien,numberOfHuman,numberOfEnergy);
    }

    @Override
    public void setTile(Tile tmp){
        ciccio.setCurrentTile(tmp);
    }
    @Override
    public void inform(String message) throws RemoteException {
        System.out.print("\n");
        ciccio.informByController(message);
    }
    @Override
    public void reportError(String error) throws RemoteException {
        ciccio.reportErrorByController(error);
    }
    @Override
    public void printListOfTileCovered(List<Tile> tiles) throws RemoteException {
        ciccio.printListOfTileCoveredByController();
    }
    @Override
    public void printListOfTileShown(List<Tile> tiles) throws RemoteException {
        ciccio.printListOfTileShownByController(tiles);
    }
    @Override
    public void printListOfGoods(List<Colour> listOfGoods) {
        ciccio.printListOfGoodsByController(listOfGoods);
    }
    @Override
    public void printCard(Card card) throws RemoteException {
        ciccio.printCardByController(card);
    }
    @Override
    public void printTile(Tile tile) throws RemoteException {
        ciccio.printTileByController(tile);
    }
    @Override
    public void printPlayerDashboard(Tile[][] dashboard) throws RemoteException {
        ciccio.printPlayerDashboardByController(dashboard);
    }
    @Override
    public void printDeck(List<Card> deck) throws RemoteException {
        ciccio.printDeckByController(deck);
    }


    /// METODI PER CHIEDERE COSE AL CLIENT DA PARTE DAL SERVER ///

    @Override
    public boolean ask(String message) throws RemoteException {
        return ciccio.askByController(message);
    }
    @Override
    public int askIndex() throws RemoteException {
        return ciccio.askIndexByController();
    }
    @Override
    public int[] askCoordinate() throws RemoteException {
        return ciccio.askCoordinateByController();

    }
    @Override
    public String askString() throws RemoteException {
        return ciccio.askStringByController();
    }


    /// METODI PER SETTARE COSE AL CLIENT RIGUARDO AL GAMEFLOW ///

    @Override
    public void updateGameState(GamePhase phase) throws RemoteException {
        ciccio.updateGameStateByController(phase);
    }

    @Override
    public void startMach() throws RemoteException {

    }
    @Override
    public GamePhase getCurrentGameState() throws RemoteException {
        return null;
    }
    @Override
    public GamePhase getGameFase() {
        return null;
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
    public int sendGameRequest(String message , int numberOfPlayer , Boolean isDemo) throws RemoteException {
        if(message.contains("CREATE")){
            try{
               return server.createNewGame(isDemo , this , nickname , numberOfPlayer);
            }catch (BusinessLogicException e){
                ciccio.reportErrorByController("Server Error"+ e.getMessage());
            }
        }

        if(message.contains("JOIN")){
            while (true){
                        Map<Integer,int[]> availableGames;
                        try {
                            availableGames = server.requestGamesList();
                        } catch (BusinessLogicException e) {
                            ciccio.reportErrorByController("Server error: " + e.getMessage());
                            return -1;
                        }
                        if (availableGames.isEmpty()) {
                            ciccio.informByController("**No available games**");
                            return -1;
                        }
                        int choice = ciccio.printAvailableGames(availableGames);
                        if (choice == 0) return 0;
                        try {
                            server.enterGame(choice, this, nickname);
                            return choice;
                        } catch (Exception e) {
                            ciccio.reportErrorByController("Cannot join: " + e.getMessage());
                            return -1;
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

        ciccio.printListOfTileShownByController(tmp);
        ciccio.informByController("Select a tile");
        int index;
        while(true){
            while (true) {
                index = askIndex();
                if (index >= 0 && index < tmp.size()) break;
                ciccio.informByController("Invalid index. Try again.");
            }
            try {
                return server.chooseUncoveredTile(gameId, nickname,tmp.get(index).getIdTile());
            } catch (BusinessLogicException e) {
                ciccio.reportErrorByController("you miss " + e.getMessage() + "select new index" );
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
        ciccio.printMyDashBoardByController();
        int[] tmp;
        while(true){
            ciccio.informByController("Choose coordinates");
            tmp = ciccio.askCoordinateByController();
            ciccio.setTileInMatrix(tile, tmp[0], tmp[1]);
            try {
                server.placeTile(gameId, nickname, tile, tmp);
                break;
            } catch (BusinessLogicException e) {
                ciccio.reportErrorByController(e.getMessage());
            }
        }
//        Dash_Matrix[tmp[0]][tmp[1]] = tile;
        ciccio.setTileInMatrix(tile , tmp[0] ,  tmp[1]);
        ciccio.printMyDashBoardByController();
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
    public void lookDeck() throws RemoteException {
        while (true) {
            ciccio.informByController("Choose deck : 1 / 2 / 3");
            int index = askIndex() + 1;

            if (index < 1 || index > 3) {
                ciccio.reportErrorByController("Invalid choice: please enter 1, 2 or 3.");
                continue;
            }

            try {
                List<Card> deck = server.showDeck(gameId, index);
                ciccio.printDeckByController(deck);
                return;
            } catch (BusinessLogicException e) {
                ciccio.reportErrorByController("Index not valid.");
            } catch (IOException e) {
                ciccio.reportErrorByController("Input error: please enter a number.");
            }
        }
    }



    @Override
    public void lookDashBoard() throws RemoteException{
        Tile[][] dashPlayer;
        String tmp;
        while(true){
             tmp = ciccio.choocePlayerByController();
            try {
                dashPlayer = server.lookAtDashBoard(gameId,tmp);
                break;
            } catch (Exception e) {
                ciccio.reportErrorByController("player not valid");
            }
        }
        ciccio.informByController("Space Ship of :" + tmp);
        ciccio.printPlayerDashboardByController(dashPlayer);
    }

    @Override
    public Tile takeReservedTile() throws RemoteException  , BusinessLogicException{
        if(ciccio.returOKAY(0,5) && ciccio.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        ciccio.printMyDashBoardByController();
        ciccio.informByController("Select a tile");
        int[] index;
        Tile tmpTile = null;
        while(true) {
            index = askCoordinate();
            if(index[0]!=0 || !ciccio.returOKAY(0 , index[1])) ciccio.informByController("Invalid coordinate");
            else if(index[1]!=5 && index[1]!=6) ciccio.informByController("Invalid coordinate");
            else break;
        }
//        view.inform("id+"+ Dash_Matrix[index[0]][index[1]].idTile);
            try {
//                view.setValidity(index[0], index[1]);
                Tile tmp = ciccio.getSomeTile(index[0], index[1]);
                ciccio.setTileInMatrix(new EmptySpace(), index[0], index[1]);
//                view.printDashShip(Dash_Matrix);
                ciccio.printMyDashBoardByController();
                tmpTile = server.getReservedTile(gameId,nickname,tmp.getIdTile());
            } catch (BusinessLogicException e) {
                ciccio.reportErrorByController("you miss " + e.getMessage() + "select new command" );
                throw new BusinessLogicException(e.getMessage());
            }
        return tmpTile;
    }
    @Override
    public void leaveGame() throws RemoteException, BusinessLogicException {
        if (gameId != 0) {
            server.LeaveGame(gameId, nickname);
            gameId = 0;
        }
    }

    @Override
    public void logOut() throws RemoteException {
        try {
            server.logOut(nickname);
        } catch (BusinessLogicException e) {
            ciccio.reportErrorByController("Server error: " + e.getMessage());
        }
        System.out.println("Goodbye!");
        System.exit(0);
    }

    @Override
    public void updateMapPosition(Map<String, Integer> Position) throws RemoteException {
        ciccio.updateMapPositionByController(Position);
    }

    @Override
    public void setStart() throws RemoteException {
        startLatch.countDown();
    }

    @Override
    public String askInformationAboutStart() throws RemoteException {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for start", e);
        }
        return "start";
    }

    @Override
    public void setIsDemo(Boolean demo) throws RemoteException{
        ciccio.setIsDemoByController(demo);
    }

    //////////////////////////////////////////////////
    //TODO:solo una bozza, scriverlo meglio. Stesso per socket
    public void enterGame(int gameId) throws RemoteException {
        try{
            server.enterGame(gameId, this , nickname);
            setGameId(gameId);
            //TODO: continuare come bisogna fare
        } catch (Exception e) {
            ciccio.reportErrorByController("you miss " + e.getMessage() );
        }
    }

    @Override
    public void updateDashMatrix(Tile[][] data) throws RemoteException {
        ciccio.newShip(data);
    }
}
