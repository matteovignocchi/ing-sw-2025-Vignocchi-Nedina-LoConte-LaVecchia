module it.polimi.ingsw.galaxytrucker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires kotlin.stdlib;
    requires java.desktop;
    requires jdk.xml.dom;

    opens it.polimi.ingsw.galaxytrucker to javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker;
}