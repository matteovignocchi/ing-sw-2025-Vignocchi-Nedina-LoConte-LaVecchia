package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Controller for the main menu GUI scene.
 * Manages the interaction for joining a game, creating a game, and logging out.
 * Sends user choices to the GUI view to be processed.
 * @author Matteo Vignocchi
 */
public class MainMenuController extends GUIController {

    @FXML private Button joinButton;
    @FXML private Button createButton;
    @FXML private Button logoutButton;

    /**
     * Initializes the main menu scene.
     * Sets action handlers for the join, create, and logout buttons to resolve
     * user menu choices via the GUI view.
     * The logout button also terminates the application.
     */
    @FXML
    public void initialize() {
        joinButton.setOnAction(event -> {
            guiView.resolveMenuChoice("2");
        });

        createButton.setOnAction(event -> {
            guiView.resolveMenuChoice("1");
        });

        logoutButton.setOnAction(event -> {
            guiView.resolveMenuChoice("3");
            System.exit(0);
        });
    }

    /**
     * Empty override of postInitialize; no additional setup is needed after loading the scene.
     */
    @Override
    public void postInitialize() {
    }

}
