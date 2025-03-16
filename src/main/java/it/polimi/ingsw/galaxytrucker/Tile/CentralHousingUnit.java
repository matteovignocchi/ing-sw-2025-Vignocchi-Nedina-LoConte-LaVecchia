package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.FullGoodsList;
import it.polimi.ingsw.galaxytrucker.Token.BrownAlien;
import it.polimi.ingsw.galaxytrucker.Token.Humans;
import it.polimi.ingsw.galaxytrucker.Token.PurpleAlien;

import java.util.ArrayList;
import java.util.List;

public class CentralHousingUnit extends Tile {
    private boolean isDestroyed;
    private List<Humans> TotHumans = new ArrayList<>();
    final int max = 2;

    public CentralHousingUnit() {
        isDestroyed = false;
        corners[0]=3;
        corners[1]=3;
        corners[2]=3;
        corners[3]=3;

    }

    public void AddHuman(Humans u) throws FullHousingList, IllegalArgumentException {
        if (TotHumans.size() == max) {
            throw new FullHousingList("HousingList is full");
        }else if (u instanceof PurpleAlien || u instanceof BrownAlien) {
            throw new IllegalArgumentException("Aliens can not stay in the human housing");
        }
        TotHumans.add(u);
    }

    public void RemoveHumans(Humans u) throws EmptyHousingList {
        if(TotHumans.size() == 1) this.destroy();
        TotHumans.remove(u);
    }

    public boolean checkStatus() {
        return isDestroyed;
    }

    public void destroy() {
        isDestroyed = true;
    }

}
