package it.polimi.ingsw.galaxytrucker.View.GUI;

public enum SceneEnum {
    MAIN_MENU("/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/fxml/BuildingPhase.fxml"),
    GAME_PHASE("/fxml/GamingPhase.fxml"),
    JOIN_GAME_MENU("/fxml/GameListMenu.fxml"),
    WAITING_QUEUE("/fxml/WaitingQueue.fxml"),
    NICKNAME_DIALOG("/fxml/NicknameDialog.fxml"),
    EXIT_PHASE("/fxml/FinalScene.fxml");
    private final String value;
    SceneEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
