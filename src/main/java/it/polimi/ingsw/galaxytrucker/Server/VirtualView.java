package it.polimi.ingsw.galaxytrucker.Server;

import it.polimi.ingsw.galaxytrucker.GameFase;
import it.polimi.ingsw.galaxytrucker.Model.Card.Card;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.rmi.Remote;
import java.util.List;
import java.util.Objects;

public interface VirtualView {
    void inform(String message) throws Exception;
    void showUpdate() throws Exception; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc
    void reportError(String error) throws Exception;
    void askDecision() throws Exception;
    void askIndex() throws Exception;
    void askCoordinates() throws Exception;
    void printList(List<Objects> pile) throws Exception;
    void setFase(GameFase fase) throws Exception;
    void printCard(Card card) throws Exception;
    void printPlayerDashboard(Tile[][] dashboard) throws Exception;
}
