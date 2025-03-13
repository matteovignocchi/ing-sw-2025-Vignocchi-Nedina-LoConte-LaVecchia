package it.polimi.ingsw.galaxytrucker.Tile;

import it.polimi.ingsw.galaxytrucker.NoEnergyLeft;

/**
 * class for the energy cell with 3 slots
 * the method remove the energy from the pile and check if is empty and the player tries to take it
 * @author Matteo Vignocchi
 */
public class TripleEnergyCell extends Tile {
    private int energy;
    public TripleEnergyCell(int a,int b,int c,int d) {
        corners[0]=a;
        corners[1]=b;
        corners[2]=c;
        corners[3]=d;
        energy = 3;
    }

    /**
     * @return how many energy I have left
     */
    public int getEnergy(){
        return energy;
    }

    /**
     * remove a energy cell from the tile
     * @throws NoEnergyLeft ricordarsi di gestire l'ecezione
     */
    public void removeEnergy() throws NoEnergyLeft {
        if(energy == 0) throw new NoEnergyLeft("Nice try...\nYou are out of energy!");
        energy--;

    }
}
