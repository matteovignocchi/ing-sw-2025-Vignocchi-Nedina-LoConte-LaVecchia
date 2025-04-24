package it.polimi.ingsw.galaxytrucker;
//cuccioli dato che tanto devo far sta cosa mentalmente abbozzo un idea dei metodi di gestione e dei corpi dei metodi
import it.polimi.ingsw.galaxytrucker.Client.VirtualServer;
import it.polimi.ingsw.galaxytrucker.Controller.Controller;
import it.polimi.ingsw.galaxytrucker.Model.Card.CardEffectException;
import it.polimi.ingsw.galaxytrucker.Server.VirtualView;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


                        ///TUTTE LE LOGICHE SONO INFONDO ALLA CLASSE///
                        ///
                        ///
public class classeDisupportoPerServer {
    List<String> listadigamedisponibili;


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

                /// METODO PER GESTIONE DELLE POSSIBILITA' , DA IMPLEMENTARE NEL SERVER///

    public List<String> sendAvailableChoices(int id) throws RemoteException {
        GameFase tmp = controller.getGameFase(id);
        List<String> listOfOptions = new ArrayList<>();
        switch (tmp) {

            case BOARD_SETUP -> {
                listOfOptions.add("get Blanket Tile");
                listOfOptions.add("take Discovery Tile");
                listOfOptions.add("Spin The Hourglass");
                listOfOptions.add("Declare Ready");
                listOfOptions.add("Watch A Deck");
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("LogOut");
            }
            //schermata dopo che ho preso una tile
            case TILE_MANAGEMENT -> {
                listOfOptions.add("return Tile");
                listOfOptions.add("place Tile");
                listOfOptions.add("LogOut");
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("RightRotate Tile");
                listOfOptions.add("LeftRotate Tile");
            }
            //schermata di attesa degli altri player
            case WAITING_FOR_PLAYERS -> {
                listOfOptions.add("Spin The Hourglass");
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("logOut");

            }
            //da qui in poi le fasi corrispondo al fatto che stiamo giocando , fase 6 è solo il primo giocatore , il pescare la carta
            //fa si che partendo dal primo un player alla volta in base alla carta pescata venga posizionato nella fase giusta e poi di nuovo
            //in fase 5 , comunque se avete dubbi chiamatemi
            //schermata attesa carta
            case WAITING_FOR_TURN  -> {
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("LogOut");
            }
            //schermata per il primo giocatore
            case DRAW_PHASE -> {
                listOfOptions.add("Draw Card");
                listOfOptions.add("LogOut");
                listOfOptions.add("Guarda Una Nave");
            }
            //da qui in poi ci sara sempre la chiamata al metodo print card ogni volta che un giocatore entra nella fase corrispettiva , e in
            //base alla fase delle opzioni diverse a schermo
            //ogni volta che il player entra in una fase chiama activate card e se la gestisce la carta , non il server
            //fase di attivazione della carta
            case CARD_EFFECT -> {
                listOfOptions.add("Watch A Ship");
                listOfOptions.add("LogOut");
                server.activateCard();
                //da qui si stampano in automatico le cose per la gestione carta senza dover creare fasi in più
            }
             //fase finale , posso solo fare logOut dalla partita
            case SCORING  -> {
                listOfOptions.add("logOut");
            }
            default -> {
                listOfOptions.add("error-404");
            }
        }
        return listOfOptions;
    }

    /// METODO GESTIONE DELLA RICHIESTA DEL PLAYER , SEMPRE DA IMPLEMENTARE NEL SERVER(ctrl c e aggiustare con gestione id)///
    public void actionManagment(String s , int id) throws RemoteException {

        String command = s.trim().toLowerCase();
        switch (command) {
            case "getblankettile"-> {
                server.getTileServer();
            }
            case "takediscoverytile"-> {
                server.getUncoveredTile();
            }
            case "returntile" -> {
                server.getBackTile();
            }
            case "placetile" -> {
                server.positionTile();
            }
            case "drawcard" -> {
                server.drawCard();
            }
            case "spinthehourglass"-> {
                server.rotateGlass();
            }
            case "declareready" -> {
                server.setReady();
            }
            case "watchadeck" -> {
                server.lookDeck();
            }
            case "watchaship" -> {
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

          /// METODO PER GESTIONE DI RICHIESTA INIZIALE SE CREO O VOGLIO ENTRARE IN UNA PARTITA///
    public void sendGameRequest(String message , int id_playey){
        if(message.contains("CREATE")){
            //metodo di creazione partita
            return;

        }
        if(message.contains("JOIN")){
            String[] msg = message.split("_");
            for(int i = 0; i < listadigamedisponibili.size(); i++){
                if(listadigamedisponibili.get(i).contains(msg[1])){
                    //metodo per controllare se posso entrare in partita
                    //metodo per aggiungere in partita
                    //se ho aggiunto lo comunnico , dico di no
                }
            }
            return;
        }
        //comunica al player i problemi del messaggio
    }

    /// DA QUI METTERO' CORPO DEL METEODO GENERALE CHIAMATO DA ACTION MANAGMENT E UNA LISTA DI TUTTE LE AZIONI CHE SERVONON///

                       ///METODI SINGOLI DI FASE BOARD_SETUP E WAITING_FOR_PLAYER///

    /// getTileServer():
                            //cosa succede ?
                            //1) il server chiama il metodo printListTile del client passandogli tutto le tile coperte
                            //   e insieme a quella print farà una inform in cui chiedere di selezionare lindice che si vuole
                            //   e chimera il metod askIndex del client
                            //2) il client seleziionato l'indice e il server una volta riceveuto farà una update allo stato del
                            //   del client mandandolo in fase TILE_MANAGMENT stampandogli a schermo la tila nella posizione standardezionerà
    /// getUncoveredTile():
                            //cosa succede?
                            //1) il server chiama il metodo printListTile del client passandogli tutto le tile coperte
                            //   e insieme a quella print farà una inform in cui chiedere di selezionare lindice che si vuole
                            //   e chimera il metod askIndex del client
                            //2) il client seleziionato l'indice e il server una volta riceveuto farà una update allo stato del
                            //   del client mandandolo in fase TILE_MANAGMENT stampandogli a schermo la tila nella posizione standardezionerà




    /// Spin The Hourglass():
                            //cosa succede?
                            //1) il server chiamerà un metodo che fa partire un timeout e incrementa un contatore alla fine di quel timeout
                            //   tutti i client si vedranno una prima inform con "clessidra girata n: count+1" (dato che count parte da 0)
                            //   e avranno una Update view che farà incrementare un valore a schermo "clessidra alla fine del thread"
                            //   il metodo sarà sincronicazzato su se stesso ,ovvero finchè il thread non finisce anche se un giocatore lo clicca
                            //  non succederà nulla
                            //  bisogna fare in modo che se count è >=2 il metodo possa essere chiamato solo da chi è ready , e se lo chiama chi non ha il flag ready non succede nulla
                            //  alla fine di ogni thread ci sarà prima della update view() un metodo che controlla count , e se è >=3 cambierà lo stato a tutti
                            //  i giocatori mettondolo a pronto e il gameFase passerà a WAITING_FOR_TURN.
                            //  il controller farà una check di tutte le regole di assemblaggio e aggiornerà di nuovo le view dei player


    /// declearReady():
                            //cosa succede?
                            //1) il server chiamerà tramite controller il setter del flag ready del player associato
                            //2) dopo il set del flag farò una update game stste mettendo il singolo player in WAITING_FOR_PLAYERS

    ///  watch a deck() :
                            //1) server chiama la print list di virtual client, inform per chiedere lindice e ask index per avere
                            // lindice indietro
                            //2) ricevuto lindice dal client il server chiamera una print list of card sul client in modo tale da stampare
                            // a schermo le carte del deck

                       ///METODI PER GESTIONE DELLA FASE TILE MANAGMENT///



    /// returTile() :
                            //1) il server riceverà dal client tramite un listeners la tile e la farà inserire nella pila
                            // delle scoperte
                            // una volta fatto ciò farà una changegamesStatus riportandolo nella fase iniziale


    /// placeTile() :
                            //1) il server chiederà le cordinate di dove si vuole posizionare la tila , il client visualizzeà
                            // in automatico la zone delle discard pile , quindi il controllo se va bene o meno lo fa in automatico
                            // il controller per dire se è prenotato o meno ( basta avere solo in addTile il controllo , lo fsccio io)
                            // una voltà ricevute le cordinate e la tile il server la darà al controller e data al controller
                            // invierà la risposta direttamente al client
                            // se la tile è stata inserità con successo tornerò in fase iniziale ,altrimenti rimango in TILE_MANAGMENT


    /// rightRotatedTile() :
                            //1) il server riceve questa richiesta , va a prenderà la tile che possiede il client associato e
                            // la girà aggiornando con viewupdate la view del client ,rimango nella stessa fase

    ///leftRotatedTile() :
                            //uguale a su


                              ///METODI DELLA CARTA///
                            //una volta che tutti i player sono ready o l'ultima clessidrà il server posizionerà tutti player in
                            // WAITING_FOR_TURN , farà fare i controlli su tutte la plance al controller e metterà al primo giocatore che ha finito
                            // in gameFase DRAW_FASE e il resto rimane in WAITING_FOR_TURN

    /// drawCard() :
                            //1) server riceve il comdando e stampa a tutti a schermo la carta pescata , poi andando in ordine
                            // di posizioni fa per ogni player
                            // -changegameFase()->CARD_EFFECT e changeView , in modo tale che un player alla volta può lavorare sulla
                            // carta e interagire con la carta , mi pare che questa logica era già implementata direttamente nelle carte , qind
                            // si potrebbe farlo direttamente da li senza dover fare giri strani
                            // una volta finito si torna in WAITING_FOR_TURN , e quando tutti sono in WAITING_FOR_TURN il server posiziona il giocatore
                            // in testa in DRAW_FASE

                    ///METODI COMUNI SEMPRE///


    /// lookDashBoard() :
                            //1) server stampa al client lista di player in game e richiede indice , il client a questo punto
                            // manda l'indice e una volta ricevuto il server stampa la nave a schermo del client dell,indice richiesto
                            // tramite un metodo apposta del client del tipo :printSHIP(TILE[][] T) e quello ci pensa poi la tui

    /// logOut() :
                            //fa uscire il player dalla parita , non fa fare la disconnessione con il server







                        }
