package it.polimi.ingsw.galaxytrucker.View.GUI;

public enum SceneEnum {
    MAIN_MENU("/polimi/ingsw/fxml/MainMenu.fxml"),
    CREATE_GAME_MENU("/polimi/ingsw/fxml/CreateGameMenu.fxml"),
    BUILDING_PHASE("/polimi/ingsw/fxml/BuildingPhase.fxml");
    private final String value;
    SceneEnum(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
