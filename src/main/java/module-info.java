module it.polimi.ingsw.galaxytrucker {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires kotlin.stdlib;
    requires jdk.xml.dom;
    requires java.smartcardio;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires java.rmi;
    requires annotations;
    requires java.naming;
    requires java.desktop;
    requires java.logging;
    requires java.compiler;

    exports it.polimi.ingsw.galaxytrucker;
    exports it.polimi.ingsw.galaxytrucker.Controller;
    exports it.polimi.ingsw.galaxytrucker.View;
    exports it.polimi.ingsw.galaxytrucker.Server;
    exports it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;

    exports it.polimi.ingsw.galaxytrucker.View.GUI;

    opens it.polimi.ingsw.galaxytrucker.Server to java.rmi;
    opens it.polimi.ingsw.galaxytrucker to javafx.fxml;
    opens it.polimi.ingsw.galaxytrucker.Model.Card to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.galaxytrucker.Client;
    opens it.polimi.ingsw.galaxytrucker.Client to java.rmi;
    opens it.polimi.ingsw.galaxytrucker.View.GUI.Controllers to javafx.fxml;
    opens it.polimi.ingsw.galaxytrucker.Controller to javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker.DtoConvention;
    opens it.polimi.ingsw.galaxytrucker.DtoConvention to javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker.Model;
    opens it.polimi.ingsw.galaxytrucker.Model to com.fasterxml.jackson.databind, javafx.fxml;
    exports it.polimi.ingsw.galaxytrucker.Exception;
    opens it.polimi.ingsw.galaxytrucker.Exception to javafx.fxml;


}