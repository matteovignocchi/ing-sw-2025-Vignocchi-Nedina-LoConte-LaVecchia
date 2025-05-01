package it.polimi.ingsw.galaxytrucker.Server;

import java.io.Serializable;

public class Message implements Serializable {
    public static final String TYPE_REQUEST = "REQUEST";
    public static final String TYPE_RESPONSE = "RESPONSE";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_NOTIFICATION = "NOTIFICATION";
    public static final String OP_LOGIN = "LOGIN";
    public static final String OP_LOGOUT = "LOGOUT";
    public static final String OP_REGISTER = "REGISTER";
    public static final String OP_UPDATE_VIEW = "UPDATE_VIEW";
    public static final String OP_ACTIONS = "ACTIONS";
    public static final String OP_LEAVE_GAME = "LEAVE_GAME";
    public static final String OP_START_GAME = "START_GAME";
    public static final String OP_LIST_GAMES = "LIST_GAMES";
    public static final String OP_PRINT_CARD = "PRINT_CARD";
    public static final String OP_PRINT_COVERED = "PRINT_COVERED";
    public static final String OP_PRINT_SHOWN = "PRINT_SHOWN";
    public static final String OP_PRINT_GOODS = "PRINT_GOODS";
    public static final String OP_PRINT_DASHBOARD = "PRINT_DASHBOARD";
    public static final String OP_GAME_PHASE = "GAME_PHASE";
    public static final String OP_PRINT_DECK = "PRINT_DECK";
    public static final String OP_PRINT_TILE = "PRINT_TILE";
    public static final String OP_GET_TILE = "GET_TILE";
    public static final String OP_GET_UNCOVERED = "GET_UNCOVERED";
    public static final String OP_GET_CARD = "GET_CARD";
    public static final String OP_INDEX = "INDEX";
    public static final String OP_COORDINATE = "COORDINATE";
    public static final String OP_STRING = "STRING";
    public static final String OP_ASK = "ASK";
    public static final String OP_RETURN_TILE = "RETURN_TILE";
    public static final String OP_POSITION_TILE = "POSITION_TILE";
    public static final String OP_ROTATE_GLASS = "ROTATE_GLASS";
    public static final String OP_SET_READY = "SET_READY";
    public static final String OP_LOOK_DECK = "LOOK_DECK";
    public static final String OP_LOOK_SHIP = "LOOK_SHIP";
    public static final String OP_SET_NICKNAME = "OP_NICKNAME";
    public static final String OP_SET_VIEW = "SET_VIEW";
    public static final String OP_SET_GAMEID = "SET_GAMEID";
    public static final String OP_MAP_POSITION = "MAP_POSITION";



    private final String messageType;
    private final String operation;
    private final Object payload;

    //utile per debug e testing così ci printa i messaggi
    private final String timestamp;

    public Message(String messageType, String operation, Object payload) {
        this.messageType = messageType;
        this.operation = operation;
        this.payload = payload;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static Message request(String operation, Object payload) {
        return new Message(TYPE_REQUEST, operation, payload);
    }

    public static Message response(Object payload) {
        return new Message(TYPE_RESPONSE, null, payload);
    }

    public static Message update(String operation, Object payload) {
        return new Message(TYPE_UPDATE, operation, payload);
    }

    public static Message error(String errorMessage) {
        return new Message(TYPE_ERROR, null, errorMessage);
    }

    public String getMessageType() {
        return messageType;
    }

    public String getOperation() {
        return operation;
    }

    public Object getPayload() {
        return payload;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public boolean isType(String type) {
        return this.messageType.equals(type);
    }

    public boolean isOperation(String operation) {
        return this.operation != null && this.operation.equals(operation);
    }

    @Override
    public String toString() {
        return String.format("Message[%s|%s|%s|%s]",
                messageType, operation, payload, timestamp);
    }


}
