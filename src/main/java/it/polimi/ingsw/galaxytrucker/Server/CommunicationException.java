package it.polimi.ingsw.galaxytrucker.Server;

//TODO: mettere tale classe in un jar condiviso, in modo che anche il client la può chiamare
public class CommunicationException extends Exception {
    public CommunicationException(String message) {
        super(message);
    }
    public CommunicationException(String msg, Throwable cause) { super(msg, cause); }
}
