package it.polimi.ingsw.galaxytrucker.View.GUI;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserInputManager {

    public CompletableFuture<String> nicknameFuture = new CompletableFuture<>();
    public CompletableFuture<String> menuChoiceFuture = new CompletableFuture<>();
    public CompletableFuture<int[]> coordinateFuture = new CompletableFuture<>();
    public CompletableFuture<Integer> indexFuture = new CompletableFuture<>();
    public CompletableFuture<Integer> rotationFuture = new CompletableFuture<>();
    public CompletableFuture<List<Object>> createGameDataFuture = new CompletableFuture<>();

    public void resetAll() {
        nicknameFuture = new CompletableFuture<>();
        menuChoiceFuture = new CompletableFuture<>();
        coordinateFuture = new CompletableFuture<>();
        indexFuture = new CompletableFuture<>();
        rotationFuture = new CompletableFuture<>();
        createGameDataFuture = new CompletableFuture<>();
    }
}
