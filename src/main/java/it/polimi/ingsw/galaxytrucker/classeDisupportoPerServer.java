package it.polimi.ingsw.galaxytrucker;
//cuccioli dato che tanto devo far sta cosa mentalmente abbozzo un idea dei metodi di gestione e dei corpi dei metodi
import it.polimi.ingsw.galaxytrucker.Client.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class classeDisupportoPerServer {


    //nei metodi se leggete controller. vuol dire che sto chiedendo al controller la fase del gioco
    Controller controller;
    VirtualServer server;
    VirtualView virtualClient;

    {
        try {
            controller = new Controller(true  , 1234);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CardEffectException e) {
            throw new RuntimeException(e);
        }
    }
    //questo è il metodo che vi chiamo ogni volta nelle fasi del gioco per sapere cosa posso e cosa non posso fare durante le fasi del gioco
    //va gestita la parte del sono il giocatore uno->si seguira una fase x in cui entra orima il giocatore 1 , poi il giocatore 2 etc...
    public List<String> sendAvaibleGames(int id) throws RemoteException {
        GameFase tmp = controller.getGameFase(id);
        List<String> listOfOptions = new ArrayList<>();
        switch (tmp) {

            //per guardare la nave degli altri non creo una fase in piu, sfrutto il fatto che il metodo viewDashboard nella tui e gui sia
            //completamente distaccato e chiamabile solo dal server
            case FASE0 -> {
                listOfOptions.add(" prendi Tile Coperta");
                listOfOptions.add(" prendi Tile Scoperta");
                listOfOptions.add(" Gira La Clessidra");
                listOfOptions.add(" Dichiarati Pronto");
                listOfOptions.add(" Guarda Un Mazzo");
                listOfOptions.add(" Guarda Una Nave");
                listOfOptions.add(" LogOut");
            }
            case FASE1 -> {
                listOfOptions.add(" restituisci Tile");
                listOfOptions.add(" posiziona Tile");
                listOfOptions.add(" LogOut");
                listOfOptions.add(" guarda Una Nave");
                listOfOptions.add(" RightRotate Tile");
                listOfOptions.add(" LeftRotate Tile");
            }
            case FASE2 -> {

            }
//            case FASE3 -> {}
            case FASE4 -> {
                listOfOptions.add(" Guarda Una Nave");
                listOfOptions.add(" LogOut");
            }
            //da qui in poi le fasi corrispondo al fatto che stiamo giocando , fase 6 è solo il primo giocatore , il pescare la carta
            //fa si che partendo dal primo un player alla volta in base alla carta pescata venga posizionato nella fase giusta e poi di nuovo
            //in fase 5 , comunque se avete dubbi chiamatemi
            case FASE5 -> {
                listOfOptions.add(" Guarda Una Nave");
                listOfOptions.add(" LogOut");
            }
            case FASE6 -> {
                listOfOptions.add(" Draw Card");
                listOfOptions.add(" LogOut");
                listOfOptions.add(" Guarda Una Nave");
            }
            //da qui in poi ci sara sempre la chiamata al metodo print card ogni volta che un giocatore entra nella fase corrispettiva , e in
            //base alla fase delle opzioni diverse a schermo
            //ogni volta che il player entra in una fase chiama activate card e se la gestisce la carta , non il server

            case FASE7 -> {
                listOfOptions.add(" Guarda Una Nave");
                listOfOptions.add(" LogOut");
                server.activateCard();
                //da qui si stampano in automatico le cose per la gestione carta senza dover creare fasi in più
            }

            case FASE13 -> {
                listOfOptions.add("logOut");
            }
            case FASE14 -> {
            }
            default -> {
            }
        }
        return listOfOptions;
    }


    //i metodi sono in void ma ci si possono passare gli id , il flusso è :
    //1 client chiama ogni volta il metodo getAvaibleGame sul suo virtualClient, che chiama sendAvaibeGames sul suo virtual server , che dopo aver
    //creato la lista la restituisce
    //una volta avuta la lista a schermo il client sceglierà il valore di riferimento al comando in loco , e chiamera
    //il metodo sendAction(choice) sul virtual client , che chiamerà a sua volta sul suo virtual server actionManagment(index sta per il
    //valore scelto dalla lista , e id perchè non so come gestite la comunicazione
    //così la maggior parte delle cose rimangono private e siamo felici tutti
    //i metodi poi vanno ampliati , se avete bisogno chiamatemi
    public void actionManagment(String s , int id) throws RemoteException {

        String command = s.trim().toLowerCase();
        switch (command) {
            case "prenditilecoperta"-> {
                //getTile prende  la tile di indice index dalla pila delle tile tramite controller , una volta presa la manda alluntente
                //che entrerà in fase 1, da li vedo cosa vuole fare
                //io chiamo i metodi come void ma dentro devono avere un qualcosa che mi manda la info al client
                //non so come volete associare gli id ai virtual server , io lascio solo lo scheletro
                server.getTileServer();
            }
            case " prenditilescoperta"-> {
                server.getUncoveredTile();
            }
            case "restituiscitile" -> {
                server.getBackTile();
            }
            case "posizionatile" -> {
                server.positionTile();
            }
            case "drawcard" -> {
                server.drawCard();
            }
            case "giralaclessidra"-> {
                server.rotateGlass();
            }
            case "dichiaratipronto" -> {
                server.setReady();
            }
            case "guardaunmazzo" -> {
                server.lookDeck();
            }
            case "guardaunanave" -> {
                server.lookDashBoard();
            }
            case "rightrotatetile" ->{
                server.rightRotatedTile();
            }
            case "leftrotatetile" -> {
                server.leftRotatedTile();
            }
            case " logout" ->{
                server.logOut();
            }
        }
    }






















}
