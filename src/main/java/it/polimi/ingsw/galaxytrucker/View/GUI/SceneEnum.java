package it.polimi.ingsw.galaxytrucker.View.GUI;

public enum SceneEnum {
    MAIN_MENU("/polimi/ingsw/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/polimi/ingsw/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/polimi/ingsw/fxml/BuildingPhase.fxml"),
    BUILDING_PHASE_DEMO("/polimi/ingsw/fxml/BuildingPhaseDemo.fxml"),
    GAME_PHASE_DEMO("/polimi/ingsw/fxml/GamePhaseDemo.fxml"),
    GAME_PHASE("/polimi/ingsw/fxml/GamePhase.fxml"),
    JOIN_GAME_MENU("polimi/ingsw/fxml/GameListMenu.fxml"),
    WAITING_QUEUE("polimi/ingsw/fxml/WaitingQueue.fxml"),
    NICKNAME_DIALOG("/polimi/ingsw/fxml/NicknameDialog.fxml");
    private final String value;
    SceneEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
