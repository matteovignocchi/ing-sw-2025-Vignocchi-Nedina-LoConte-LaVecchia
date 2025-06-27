package it.polimi.ingsw.galaxytrucker.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.*;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        String host = (args.length > 0 && !args[0].isBlank())
                ? args[0]
                : getLocalHostAddress();

        int rmiPort = (args.length > 1)
                ? Integer.parseInt(args[1])
                : 1099;

        int socketPort = (args.length > 2)
                ? Integer.parseInt(args[2])
                : 30001;

        System.setProperty("java.rmi.server.hostname", host);
        GameManager gameManager = new GameManager();

        Registry registry = LocateRegistry.createRegistry(rmiPort);
        registry.rebind("RmiServer", new ServerRmi(gameManager));
        System.out.printf("Server RMI ready on port %d%n", rmiPort);
        ServerSocketMain socketMain = new ServerSocketMain(gameManager, socketPort);
        Thread socketThread = new Thread(socketMain, "Socket-Listener");
        socketThread.start();
        System.out.printf("Server SOCKET ready on port %d%n", socketPort);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown in progress…");
            socketThread.interrupt();
            try {
                socketThread.join();
            } catch (InterruptedException ignored) {}
            System.out.println("Server terminated.");
        }));
    }

    /**
     * Restituisce l’indirizzo IPv4 locale non-loopback,
     * o localhost se fallisce.
     */
    private static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}

