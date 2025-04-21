package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;

import java.util.List;

public interface VirtualView {
    void showUpdate() throws Exception; //sicuramente ci andrà un type di update che voglio, forse faccio direttamente showDashboard show fligh ecc
    void reportError(String error) throws Exception;
    void ask(String question) throws Exception;
    void printPileOfTile(List<Tile> pile) throws Exception;


}
