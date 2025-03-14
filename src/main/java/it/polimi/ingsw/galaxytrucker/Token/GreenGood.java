package it.polimi.ingsw.galaxytrucker.Token;

public class GreenGood implements Good {
    final int price = 2;
    @Override
    public int GetPrice(){
        return price;
    }
}
