module it.polimi.ingsw.galaxytrucker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens it.polimi.ingsw.galaxytrucker to javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker;
    exports it.polimi.ingsw.galaxytrucker.Tile;
    opens it.polimi.ingsw.galaxytrucker.Tile to javafx.fxml;
}