package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.Model.Colour;

import java.util.List;
import java.util.Scanner;

public class PlayerView {

    private int idPlayer;
    private  Scanner scanner = new Scanner(System.in);

    public PlayerView(int idPlayer) {
        this.idPlayer = idPlayer;
    }
    public int getId() {
        return idPlayer;
    }

    public void inform(String message) {
        System.out.println(message);
    }

    public  boolean ask(String x){
        System.out.print(x + " (sì/no): ");
        String risposta = scanner.nextLine().trim().toLowerCase();
      while(true) {
          if (risposta.equals("sì") || risposta.equals("si")) {
              return true;
          } else if (risposta.equals("no")) {
              return false;
          }
          else {
          System.out.print("rispondere con si o no");
          }
      }
    }

    public void printFirePower(float power){
        System.out.print(power);
    }
    public void printEnginePower(int x){
        System.out.print(x);
    }
    public void printNumCredits(int credits){
        System.out.print(credits);
    }

    public int[] askCoordinate(){
        int[] cordinate = new int[2];
        System.out.print("selezionare la cella di riferimento ");
        System.out.print("inserire la riga");
        cordinate[0] = scanner.nextInt();
        System.out.print("inserire la colonna");
        cordinate[1] = scanner.nextInt();
        return cordinate;
    }

    public int askIndex(){
        return 3;
    }

    public void printListOfGoods(List<Colour> listOfGoods){}



    //public boolean askPlayerDecision() {
    // ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    //    ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
    //    Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
    //    choice.setTitle("Choose your action");
    //    choice.setHeaderText(null);
    //    Optional<ButtonType> result = choice.showAndWait();
    //    return result.isPresent() && result.get().equals(buttonYes);
    //}


    //    //metodo gestione di che posso fare
//    private static void handleMainActionPhase() throws Exception{
//        //chiedo ad oleg domani, prima non devo fare l'update della view perchè prima vedo
//        //la mainActionPhase poi scelgo cosa fare
//        view.updateState(FASE0);
//        view.inform("Possible actions:");
//        List<String> possibleActions = virtualClient.getAvailableAction();
//
//        for(int i = 0 ; i < possibleActions.size(); i++){
//            view.inform((i+1)+"."+possibleActions.get(i));
//        }
//        int choice = virtualClient.askIndex();
//        //chiedere perchè send action non è un void ma è un string
//        String result = virtualClient.sendAction(possibleActions.get(choice-1));
//    }

//    private static void handleChoosingCoveredTile() throws Exception{
//        view.updateState(FASE1);
//        //per fare più easy possiamo che ci da soltanto il numero di tessere coperte, che tanto passo da 151 circa,
//        //quindi mandare ogni volta la lista che risulta pesante
//        List<Tile> pile = virtualClient.getPileOfTile();
//        view.printList("pile", pile);
//        //VERSIONE 2
//        //int totalTile = virtualClient.getNumOfTile();
//        //view.printCovered(totalTile);
//        view.inform("Choose one of covered tile and give the index");
//        int index = virtualClient.askIndex();
//        //l'ho pensato così forse è sbagliato
//        Tile tile = virtualClient.getTile(index -1);
//        view.printTile(tile);
//        view.inform("Possible actions:");
//        List<String> possibleActions = virtualClient.getAvailableAction();
//        for(int i = 0 ; i < possibleActions.size(); i++){
//            view.inform((i+1)+"."+possibleActions.get(i));
//        }
//        int choice = virtualClient.askIndex();
//        String result = virtualClient.sendAction(possibleActions.get(choice -1));
//
//    }
//
//    private static void handleBuildingPhase() throws Exception{
//        view.updateState(FASE2);
//        view.inform("Choose one of slots and give the indexes");
//        int[] coordinate = view.askCordinate();
//        //qua credo ci vada il send coordinates però dobbiamo creare il metodo
//    }
//
}
