module it.polimi.ingsw.galaxytrucker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires kotlin.stdlib;
    requires jdk.xml.dom;
    requires java.smartcardio;
    requires com.fasterxml.jackson.databind;

    requires java.rmi;
    exports it.polimi.ingsw.galaxytrucker.Server;
    opens it.polimi.ingsw.galaxytrucker.Server to java.rmi;

    requires annotations;
    requires java.naming;
    requires java.desktop;
    requires java.logging;

    opens it.polimi.ingsw.galaxytrucker to javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker;

}