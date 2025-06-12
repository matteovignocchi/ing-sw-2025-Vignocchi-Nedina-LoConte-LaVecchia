package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Exception.BusinessLogicException;
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
    private transient ClientController clientController;
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
        this.clientController = clientController;
    }

    @Override
    public boolean askWithTimeout(String question) throws RemoteException{
        return clientController.askWithTimeoutByController(question);
    }

    @Override
    public int[] askCoordsWithTimeout() throws RemoteException{
        return clientController.askCoordinatesWithTimeoutByController();
    }

    @Override
    public Integer askIndexWithTimeout() throws RemoteException{
        return clientController.askIndexWithTimeoutByController();
    }


    /// METODI PER PRINTARE A CLIENT ///

    @Override
    public void showUpdate(String nickname, double firePower, int powerEngine, int credits, boolean purpleAline, boolean brownAlien, int numberOfHuman, int numberOfEnergy) throws RemoteException {
        clientController.showUpdateByController(nickname,firePower,powerEngine,credits,purpleAline,brownAlien,numberOfHuman,numberOfEnergy);
    }

    @Override
    public void setTile(String jsonTmp){
        clientController.setCurrentTile(jsonTmp);
    }
    @Override
    public void inform(String message) throws RemoteException {
        clientController.informByController(message);
    }
    @Override
    public void reportError(String error) throws RemoteException {
        clientController.reportErrorByController(error);
    }
    @Override
    public void printListOfTileCovered(String jsonTiles) throws RemoteException {
        clientController.printListOfTileCoveredByController();
    }
    @Override
    public void printListOfTileShown(String jsonTiles) throws RemoteException {
        clientController.printListOfTileShownByController(jsonTiles);
    }
    @Override
    public void printListOfGoods(List<String> listOfGoods) {
        clientController.printListOfGoodsByController(listOfGoods);
    }
    @Override
    public void printCard(String jsonCard) throws RemoteException {
        clientController.printCardByController(jsonCard);
    }
    @Override
    public void printTile(String jsonTile) throws RemoteException {
        clientController.printTileByController(jsonTile);
    }
    @Override
    public void printPlayerDashboard(String[][] jsonDashboard) throws RemoteException {
        clientController.printPlayerDashboardByController(jsonDashboard);
    }
    @Override
    public void printDeck(String jsonDeck) throws RemoteException {
        clientController.printDeckByController(jsonDeck);
    }


    /// METODI PER CHIEDERE COSE AL CLIENT DA PARTE DAL SERVER ///

    @Override
    public Boolean ask(String message) throws RemoteException {
        return clientController.askByController(message);
    }
    @Override
    public Integer askIndex() throws IOException, InterruptedException {
        return clientController.askIndexByController();
    }
    @Override
    public int[] askCoordinate() throws RemoteException {
        return clientController.askCoordinateByController();

    }
    @Override
    public String askString() throws RemoteException {
        return clientController.askStringByController();
    }


    /// METODI PER SETTARE COSE AL CLIENT RIGUARDO AL GAMEFLOW ///

    @Override
    public void updateGameState(String phase) throws RemoteException {
        clientController.updateGameStateByController(phase);
    }

    @Override
    public void startMach() throws RemoteException {

    }
    //TODO:sistemare, vedere se servono
//    @Override
//    public GamePhase getCurrentGameState() throws RemoteException {
//        return null;
//    }
//    @Override
//    public GamePhase getGameFase() {
//        return null;
//    }


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
    public int sendGameRequest(String message , int numberOfPlayer , Boolean isDemo) throws IOException, InterruptedException {
        if(message.contains("CREATE")){
            try{
               return server.createNewGame(isDemo , this , nickname , numberOfPlayer);
            }catch (BusinessLogicException e){
                clientController.reportErrorByController("Server Error"+ e.getMessage());
            }
        }

        if(message.contains("JOIN")){
            while (true){
                Map<Integer,int[]> availableGames;
                try {
                    availableGames = server.requestGamesList();
                } catch (BusinessLogicException e) {
                    clientController.reportErrorByController("Server error: " + e.getMessage());
                    return -1;
                }
                if (availableGames.isEmpty()) {
                    clientController.informByController("**No available games**");
                    return -1;
                }
                int choice = clientController.printAvailableGames(availableGames);
                if (choice == 0) return 0;
                try {
                    server.enterGame(choice, this, nickname);
                    return choice;
                } catch (Exception e) {
                    clientController.reportErrorByController("Cannot join: " + e.getMessage());
                    return -1;
                }
            }
        }
        return 0;
    }

    /// COMANDI CHE CHIAMO SUL SERVER DURANTE LA PARTITA ///

    @Override
    public String getTileServer() throws RemoteException , BusinessLogicException {
            try {
                String tmp = server.getCoveredTile(gameId, nickname);
                return tmp;
            } catch (BusinessLogicException e) {
                throw new RemoteException(e.getMessage());
            }
        }
//assicurarsi che funziona
    @Override
    public String getUncoveredTile() throws BusinessLogicException, IOException, InterruptedException {
        String tmp;
        try {
            tmp = server.getUncoveredTilesList(gameId, nickname);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to fetch tile list: " + e.getMessage());
        }
        if (tmp == null || tmp.equals("PIEDONIPRADELLA")) {
            throw new BusinessLogicException("The list of shown tiles is empty.");
        }

        int size =  clientController.printListOfTileShownByController(tmp);
        clientController.informByController("Select a tile");
        while (true) {
            Integer indexObj = askIndex();
            if(indexObj == null) { return null; }
            int index = indexObj;
            if (index >= 0 && index < size) {
                try {
                    return server.chooseUncoveredTile(gameId, nickname, clientController.clientTileFromList(index));
                } catch (BusinessLogicException e) {
                    clientController.reportErrorByController("You missed: " + e.getMessage() + ". Select a new index.");
                }
            } else {
                clientController.informByController("Invalid index. Try again.");
            }
        }
    }

    @Override
    public void getBackTile(String jsontile) throws RemoteException , BusinessLogicException{
            try {
                server.dropTile(gameId,nickname,jsontile);
            } catch (BusinessLogicException e) {
                throw new BusinessLogicException(e.getMessage());
            }
        }

    @Override
    public void positionTile(String jsonTile) throws RemoteException{
        clientController.printMyDashBoardByController();
        clientController.informByController("Choose coordinate!");
        int[] tmp;
        tmp = clientController.askCoordinateByController();
        if(tmp == null) { return; }
        clientController.setTileInMatrix(jsonTile, tmp[0], tmp[1]);
        try {
            server.placeTile(gameId, nickname, jsonTile, tmp);
        } catch (BusinessLogicException e) {
            return;
        }
//      clientController.setTileInMatrix(jsonTile , tmp[0] ,  tmp[1]);//TODO:potrebbe essere ridondante
        clientController.printMyDashBoardByController();
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
    public void lookDeck() throws IOException, InterruptedException {
        while (true) {
            clientController.informByController("Choose deck : 1 / 2 / 3");
            Integer indexObj = askIndex();
            if(indexObj == null) { return; }

            int index = indexObj + 1;
            if (index < 1 || index > 3) {
                clientController.reportErrorByController("Invalid choice: please enter 1, 2 or 3.");
                continue;
            }

            try {
                String deck = server.showDeck(gameId, index);
                clientController.printDeckByController(deck);
                return;
            } catch (BusinessLogicException e) {
                clientController.reportErrorByController("Index not valid.");
            } catch (IOException e) {
                clientController.reportErrorByController("Input error: please enter a number.");
            }
        }
    }



    @Override
    public void lookDashBoard() throws RemoteException,IOException, InterruptedException {
        String tmp;

        tmp = clientController.choosePlayerByController();
        if(tmp == null) return;

       String[][] dashPlayer;
        try {
            dashPlayer = server.lookAtDashBoard(gameId,tmp);
        } catch (Exception e) {
            clientController.reportErrorByController("player not valid");
            return;
        }
        clientController.informByController("Space Ship of :" + tmp);
        clientController.printPlayerDashboardByController(dashPlayer);
    }

    @Override
    public String takeReservedTile() throws RemoteException  , BusinessLogicException{
        if(clientController.returOKAY(0,5) && clientController.returOKAY(0,6)) {
            throw new BusinessLogicException("There is not any reserverd tile");
        }
        clientController.printMyDashBoardByController();
        clientController.informByController("Select a tile");
        int[] index;
        String tmpTile = null;
        while(true) {
            index = clientController.askCoordinateByController();
            if (index == null) {return null;}
            if(index[0]!=0 || clientController.returOKAY(0 , index[1])) clientController.informByController("Invalid coordinate");
            else break;
        }
            try {
                int tmp = clientController.returnIdOfTile(index[0], index[1]);
                clientController.setTileInMatrix("PIEDINIPRADELLA", index[0], index[1]);
                clientController.resetValidityByController(index[0], index[1]);
                clientController.printMyDashBoardByController();
                tmpTile = server.getReservedTile(gameId,nickname,tmp);
            } catch (BusinessLogicException e) {
                clientController.reportErrorByController("you miss " + e.getMessage() + "select new command" );
                throw new BusinessLogicException(e.getMessage());
            }
        return tmpTile;
    }
    @Override
    public void leaveGame() throws RemoteException, BusinessLogicException {
        if (gameId != 0) {
            try {
                server.LeaveGame(gameId, nickname);
            } catch (BusinessLogicException e) {
            }
            gameId = 0;
        }
    }

    @Override
    public void logOut() throws RemoteException {
        try {
            server.logOut(nickname);
        } catch (BusinessLogicException e) {
            clientController.reportErrorByController("Server error: " + e.getMessage());
        }
        System.out.println("Goodbye!");
        System.exit(0);
    }

    @Override
    public void updateMapPosition(Map<String, Integer> Position) throws RemoteException {
        clientController.updateMapPositionByController(Position);
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
        clientController.setIsDemoByController(demo);
    }

    //////////////////////////////////////////////////
    //TODO:solo una bozza, scriverlo meglio. Stesso per socket
    public void enterGame(int gameId) throws RemoteException {
        try{
            server.enterGame(gameId, this , nickname);
            setGameId(gameId);
            //TODO: continuare come bisogna fare
        } catch (Exception e) {
            clientController.reportErrorByController("you miss " + e.getMessage() );
        }
    }

    @Override
    public void updateDashMatrix(String[][] data) throws RemoteException {
        clientController.newShip(data);
    }
}
