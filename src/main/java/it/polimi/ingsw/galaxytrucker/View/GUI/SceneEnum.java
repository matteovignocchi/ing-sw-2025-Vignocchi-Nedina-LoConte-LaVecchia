package it.polimi.ingsw.galaxytrucker.View.GUI;

public enum SceneEnum {
    MAIN_MENU("/polimi/ingsw/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/polimi/ingsw/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/polimi/ingsw/fxml/BuildingPhase.fxml"),
    JOIN_GAME_MENU("polimi/ingsw/fxml/GameListMenu.fxml"),
    WAITING_QUEUE("polimi/ingsw/fxml/WaitingQueue.fxml");
    private final String value;
    SceneEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
