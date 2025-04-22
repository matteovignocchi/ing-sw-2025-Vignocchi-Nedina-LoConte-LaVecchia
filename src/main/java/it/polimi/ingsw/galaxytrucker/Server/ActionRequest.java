package it.polimi.ingsw.galaxytrucker.Server;

import java.io.Serializable;

public class ActionRequest implements Serializable {
    private final String actionType;
    private final Object payload;
    public ActionRequest(String actionType, Object payload) {
        this.actionType = actionType;
        this.payload = payload;
    }

    // Getters
    public String getActionType() { return actionType; }
    public Object getPayload() { return payload; }
}
