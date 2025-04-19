package it.polimi.ingsw.galaxytrucker.Server;

import java.util.Scanner;

public class TUIView {
    private int idPlayer;
    private int server;
    private Scanner scanner = new Scanner(System.in);


    //per ora lascio il server come int
    public TUIView(int idPlayer, int server) {
        this.idPlayer = idPlayer;
        this.server = server;
    }
    public void inform(String message) {
        System.out.println("> " + message + "\n");
    }

    public void ask(String x){
        boolean flag = true;
        System.out.print(">" + x + " (Yes/No): \n");
        String response = scanner.nextLine().trim().toLowerCase();
        while(flag) {
            if (response.equals("yes")) {
                //server.sendChoice(playerId, true);
                flag = false;
            } else if (response.equals("no")) {
                //server.sendChoice(playerId, false);
                flag = false;
            }
            else {
                System.out.print("> The response entered is invalid. Try again: \n");
            }
        }
    }
    public void askCoordinate(){
        int[] cordinate = new int[2];
        System.out.print("selezionare la cella di riferimento ");
        System.out.print("inserire la riga");
        cordinate[0] = scanner.nextInt();
        System.out.print("inserire la colonna");
        cordinate[1] = scanner.nextInt();
        //server.sendCoordinate(playerId, coordinate);
    }

    public void printFirePower(float power){
        System.out.print("> " + power +"\n");
    }
    public void printEnginePower(int x){
        System.out.print("> "+x+"\n");
    }
    public void printNumCredits(int credits){
        System.out.print("> "+credits+"\n");
    }


}
