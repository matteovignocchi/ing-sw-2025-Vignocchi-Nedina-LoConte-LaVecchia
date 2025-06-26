package it.polimi.ingsw.galaxytrucker.Client;
import it.polimi.ingsw.galaxytrucker.Server.VirtualServer;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIStartupConfig;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import it.polimi.ingsw.galaxytrucker.View.TUIView;
import it.polimi.ingsw.galaxytrucker.View.View;
import javafx.application.Application;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.Set;


public class ClientMain {

    public static void main(String[] args) throws RemoteException, IOException, NotBoundException {
        // Banner ASCII
        System.out.println(
                " _____       _                    _____               _             \n" +
                        "|  __ \\     | |                  |_   _|             | |            \n" +
                        "| |  \\/ __ _| | __ ___  ___   _    | |_ __ _   _  ___| | _____ _ __ \n" +
                        "| | __ / _` | |/ _` \\ \\/ / | | |   | | '__| | | |/ __| |/ / _ \\ '__|\n" +
                        "| |_\\ \\ (_| | | (_| |>  <| |_| |   | | |  | |_| | (__|   <  __/ |   \n" +
                        " \\____/\\__,_|_|\\__,_/_/\\_\\\\__, |   \\_/_|   \\__,_|\\___|_|\\_\\___|_|   \n" +
                        "                           __/ |                                    \n" +
                        "                          |___/                                     \n"
        );


        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the server IP address:");
        String host = readHost(scanner);

        int rmiPort    = 1099;
        int socketPort = 30001;

        int protocolChoice = readChoice(
                scanner,
                "Choose the type of protocol:\n 1 - RMI\n 2 - SOCKET",
                Set.of("1", "2")
        );

        int viewChoice = readChoice(
                scanner,
                "Choose the type of view:\n 1 - TUI\n 2 - GUI",
                Set.of("1", "2")
        );

        try {
            it.polimi.ingsw.galaxytrucker.Client.VirtualView virtualClient;
            if (protocolChoice == 1) {
                Registry registry = LocateRegistry.getRegistry(host, rmiPort);
                VirtualServer server = (VirtualServer) registry.lookup("RmiServer");
                virtualClient = new VirtualClientRmi(server);
            } else {
                virtualClient = new VirtualClientSocket(host, socketPort);
            }

            if (viewChoice == 1) {
                View view = new TUIView();
                new ClientController(view, virtualClient).start();
            } else {
                GUIStartupConfig.protocolChoice = protocolChoice;
                GUIStartupConfig.host = host;
                GUIStartupConfig.port = socketPort;
                GUIStartupConfig.virtualClient = virtualClient;
                Application.launch(GUIView.class);
            }

        } catch (Exception e) {
            System.err.println("Connection Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_BOLD   = "\u001B[1m";
    private static final String ANSI_RESET  = "\u001B[0m";

    private static int readChoice(Scanner scanner, String prompt, Set<String> validOptions) {
        while (true) {
            System.out.println(prompt);
            System.out.print("");
            String line = scanner.nextLine().trim();
            if (validOptions.contains(line)) {
                return Integer.parseInt(line);
            }
            System.out.println(ANSI_RED + "[ERROR] Invalid choice. Please enter " + ANSI_BOLD + "1" + ANSI_RESET + ANSI_RED + " or " + ANSI_BOLD + "2" + ANSI_RESET);
        }
    }

    private static String readHost(Scanner scanner) {
        while (true) {
            System.out.print("Inserisci l'indirizzo IP del server: ");
            String host = scanner.nextLine().trim();
            try {
                InetAddress.getByName(host);
                return host;
            } catch (UnknownHostException e) {
                System.out.println(ANSI_RED + "[ERROR] Invalid or unreachable address, try again!\n" + ANSI_RESET);
            }
        }
    }
}


