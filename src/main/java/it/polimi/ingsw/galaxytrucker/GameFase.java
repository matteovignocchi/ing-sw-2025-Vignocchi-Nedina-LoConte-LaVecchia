package it.polimi.ingsw.galaxytrucker;

public enum GameFase {
    FASE0, //fase iniziale quando costruisci(ho la scelte di prendere la tile dalle coperte, scoperte, posso girare la clessidra, posso dichiararmi pronto, guarda nave altri giocatori, guarda mazzo, e uscire(comune a tutte))
    FASE1, //prima chiedo indice, ho preso la tile dalle coperte o dalle scoperte, decido dove metterla
    FASE2, //decido di metterla sulla dashboard, decido prima posizione poi l'orientazione
    FASE3, //ho deciso di mettere la tile nelle prenotate
    FASE4, //clicco pronto e aspetto gli altri, nelle scelte ho sempre guardare le altre navi e uscire del gioco
    FASE5, //adesso stiamo giocando,(sei primo) hai le scelte del tipo pesca carta, guarda la nave avversiaria, esci
    FASE6, //chiedo indice, fase visualizzazione nave avversaria
    FASE7, //se non sono primo, aspetto gli altri che fanno azioni, uscirà la print della carta, l'unica cosa che posso fare e aspettare che gli altri decidano
    FASE8, //scelte all'interno della carta con la print della carta(di nuovo), scelta cosa fare della carta
    FASE9, //tipo aggiungere scegliere il pianeta, goods e possible actions
    FASE10, //scelgo la cella giusta della casella, scambio goods
    FASE11, //fase meteoriti/cannonate, lancio dati e vedo cosa succede, quindi se hitta scelta scudi ecc
    FASE12, //scelta energie all'interno delle energy cell
    FASE13, //fase finale, con posizioni di vittoria, uniche scelte sono crea nuova partita, entra in una partita, logout
    FASE14,
    FASE15
}
