package it.polimi.ingsw.galaxytrucker.Token;

public class YellowGood implements Good {
    final int price = 3;
    @Override
    public int GetPrice(){
        return price;
    }
}
