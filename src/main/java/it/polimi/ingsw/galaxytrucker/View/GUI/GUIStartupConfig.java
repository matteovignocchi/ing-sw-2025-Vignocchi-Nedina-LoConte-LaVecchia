package it.polimi.ingsw.galaxytrucker.View.GUI;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;

/**
 * Static configuration holder used during GUI startup.
 * Stores the network protocol choice, server host and port,
 * and the VirtualView client instance shared across the GUI components.
 * All fields are static and accessible globally within the GUI context.
 * @author Oleg Nedina
 */
public class GUIStartupConfig {
    public static int protocolChoice;
    public static String host;
    public static int port;
    public static VirtualView virtualClient;
}
