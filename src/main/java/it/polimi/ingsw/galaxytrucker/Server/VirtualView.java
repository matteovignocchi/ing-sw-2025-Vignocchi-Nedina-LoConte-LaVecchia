package it.polimi.ingsw.galaxytrucker.Server;

public interface VirtualView {
    void showUpdate() throws Exception; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc
    void reportError(String error) throws Exception;
    void ask(String question) throws Exception;

}
