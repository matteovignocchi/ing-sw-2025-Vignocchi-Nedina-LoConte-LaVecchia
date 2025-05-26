package it.polimi.ingsw.galaxytrucker;

import java.util.ArrayList;
import java.util.List;

public class TileDTO {
    public String type;
    public int a, b, c, d;
    public int id;
    public boolean idDouble;
    public int max;
    public boolean advance;
    public String human;
    public List<String> goods = new ArrayList<>();
    public List<String> tokens = new ArrayList<>();
    public List<Integer> protectedCorners = new ArrayList<>();
}