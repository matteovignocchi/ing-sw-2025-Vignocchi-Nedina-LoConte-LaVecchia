package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.Token.Humans;

import java.util.ArrayList;
import java.util.List;

public class BrownAlienUnit extends Tile {



    private List<Humans> TotHumans = new ArrayList<>();
    int max;
    boolean alienpresent = false;



    public BrownAlienUnit(int a, int b, int c, int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        max = 2;
    }




}
