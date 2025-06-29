package it.polimi.ingsw.galaxytrucker.View.GUI;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages asynchronous user input operations in the GUI.
 * Each input is represented as a {@link CompletableFuture} to be completed
 * when the user provides the corresponding input.
 * This class allows resetting all pending inputs to start fresh for new interactions.
 * @author Oleg Nedina
 * @author Matteo Vignocchi
 */
public class UserInputManager {

    public CompletableFuture<String> nicknameFuture = new CompletableFuture<>();
    public CompletableFuture<String> menuChoiceFuture = new CompletableFuture<>();
    public CompletableFuture<int[]> coordinateFuture = new CompletableFuture<>();
    public CompletableFuture<Integer> indexFuture = new CompletableFuture<>();
    public CompletableFuture<Integer> rotationFuture = new CompletableFuture<>();
    public CompletableFuture<List<Object>> createGameDataFuture = new CompletableFuture<>();

    /**
     * Resets all futures to new instances.
     * This clears any existing pending input and prepares for fresh input requests.
     */
    public void resetAll() {
        nicknameFuture = new CompletableFuture<>();
        menuChoiceFuture = new CompletableFuture<>();
        coordinateFuture = new CompletableFuture<>();
        indexFuture = new CompletableFuture<>();
        rotationFuture = new CompletableFuture<>();
        createGameDataFuture = new CompletableFuture<>();
    }
}
