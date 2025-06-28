package it.polimi.ingsw.galaxytrucker.Server;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.*;

/**
 * Application entry point for the Galaxy Trucker server.
 * Initializes the GameManager, sets up the RMI registry and socket listener
 * on configurable ports, and installs a shutdown hook to cleanly stop both services.
 *
 * @author Francesco Lo Conte
 * @author Gabriele La Vecchia
 */
public class ServerMain {

    /**
     * Starts the RMI and socket servers, using optional command-line overrides for host and ports.
     * @param args optional arguments: [0]=host (defaults to local address), [1]=RMI port (defaults to 1099), [2]=socket port (defaults to 30001)
     * @throws Exception if server initialization fails
     */
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
     * Retrieves the machine’s non-loopback IPv4 address, or falls back to localhost.
     * @return the local host IPv4 address, or "127.0.0.1" if unavailable
     */
    private static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}

