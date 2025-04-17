package it.polimi.ingsw.galaxytrucker;

import it.polimi.ingsw.galaxytrucker.Server.Model.Colour;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Optional;
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

}
