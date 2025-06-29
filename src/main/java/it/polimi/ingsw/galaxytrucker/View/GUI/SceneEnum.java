package it.polimi.ingsw.galaxytrucker.View.GUI;

/**
 * Enumeration of all GUI scenes in the application.
 * Each enum constant corresponds to an FXML file that defines
 * the layout and components of a particular GUI scene.
 * Available scenes include:
 * - MAIN_MENU: the main menu of the game
 * - CREATE_GAME_MENU: scene for creating a new game
 * - BUILDING_PHASE: scene during ship building
 * - GAME_PHASE: main gameplay scene
 * - JOIN_GAME_MENU: scene for joining existing games
 * - WAITING_QUEUE: waiting room scene
 * - NICKNAME_DIALOG: dialog for entering player nickname
 * - EXIT_PHASE: final scene after game ends
 * Each scene holds the relative path to its FXML file.
 * @author Matteo Vignocchi
 */
public enum SceneEnum {
    MAIN_MENU("/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/fxml/BuildingPhase.fxml"),
    GAME_PHASE("/fxml/GamingPhase.fxml"),
    JOIN_GAME_MENU("/fxml/GameListMenu.fxml"),
    WAITING_QUEUE("/fxml/WaitingQueue.fxml"),
    NICKNAME_DIALOG("/fxml/NicknameDialog.fxml"),
    EXIT_PHASE("/fxml/FinalScene.fxml"),
    CHAT("/fxml/Chat.fxml");
    private final String value;

    SceneEnum(final String value) {
        this.value = value;
    }

    /**
     * Returns the FXML file path associated with this scene.
     * @return the relative path to the FXML resource
     */
    public String value() {
        return value;
    }
}
