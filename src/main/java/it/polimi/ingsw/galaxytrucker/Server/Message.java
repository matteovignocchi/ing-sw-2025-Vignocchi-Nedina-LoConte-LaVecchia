package it.polimi.ingsw.galaxytrucker.Server;

import java.io.Serializable;

public class Message implements Serializable {
    public static final String TYPE_REQUEST = "REQUEST";
    public static final String TYPE_RESPONSE = "RESPONSE";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_NOTIFICATION = "NOTIFICATION";
    public static final String OP_LOGIN = "LOGIN";
    public static final String OP_REGISTER = "REGISTER";
    public static final String OP_CREATE_GAME = "CREATE_GAME";
    public static final String OP_JOIN_GAME = "JOIN_GAME";
    public static final String OP_LEAVE_GAME = "LEAVE_GAME";
    public static final String OP_START_GAME = "START_GAME";
    public static final String OP_LIST_GAMES = "LIST_GAMES";

    public static final String OP_GAME_ACTION = "GAME_ACTION";
    public static final String OP_GET_BOARD = "GET_BOARD";
    public static final String OP_GET_TILES = "GET_TILES";
    public static final String OP_GET_CARDS = "GET_CARDS";


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
