package it.polimi.ingsw.galaxytrucker.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientTile {
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

    public void rotateRight() {
        int temp = d;
        d = c;
        c = b;
        b = a;
        a = temp;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(3);
            protectedCorners.set(3, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(0));
            protectedCorners.set(0, tmp);
        }
    }

    public void rotateLeft() {
        int temp = a;
        a = b;
        b = c;
        c = d;
        d = temp;

        if (protectedCorners != null && protectedCorners.size() == 4) {
            int tmp = protectedCorners.get(0);
            protectedCorners.set(0, protectedCorners.get(1));
            protectedCorners.set(1, protectedCorners.get(2));
            protectedCorners.set(2, protectedCorners.get(3));
            protectedCorners.set(3, tmp);
        }
    }
}

