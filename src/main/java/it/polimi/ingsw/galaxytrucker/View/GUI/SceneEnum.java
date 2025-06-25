package it.polimi.ingsw.galaxytrucker.View.GUI;

public enum SceneEnum {
    MAIN_MENU("/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/fxml/BuildingPhase.fxml"),
    //BUILDING_PHASE_DEMO("/fxml/BuildingPhaseDemo.fxml"),
    //GAME_PHASE_DEMO("/fxml/GamePhaseDemo.fxml"),
    GAME_PHASE("/fxml/GamingPhase.fxml"),
    JOIN_GAME_MENU("/fxml/GameListMenu.fxml"),
    WAITING_QUEUE("/fxml/WaitingQueue.fxml"),
    NICKNAME_DIALOG("/fxml/NicknameDialog.fxml"),
    PRINT_GOODS("/fxml/PrintListOfGood.fxml"),

    EXIT_PHASE("/fxml/Finale.fxml");
    private final String value;
    SceneEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
