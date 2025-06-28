package it.polimi.ingsw.galaxytrucker.Client;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a generic message object exchanged between the client and server in the Galaxy Trucker game.
 * This class encapsulates a protocol for communication over sockets via
 * {@link it.polimi.ingsw.galaxytrucker.Client.VirtualClientSocket} and
 * {@link it.polimi.ingsw.galaxytrucker.Server.ClientHandler}. It serves as a unified structure for transmitting:
 * Client requests (e.g., {@code OP_LOGIN}, {@code OP_GET_TILE})
 * Server responses (containing payloads or errors)
 * Server-initiated updates (e.g., game state changes)
 * Notifications (like disconnection warnings)
 * @author Matteo Vignocchi
 */
public class Message implements Serializable {

    // Message types
    public static final String TYPE_REQUEST = "REQUEST";
    public static final String TYPE_RESPONSE = "RESPONSE";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_NOTIFICATION = "NOTIFICATION";

    // Message operations
    public static final String OP_LOGIN = "LOGIN";
    public static final String OP_LOGOUT = "LOGOUT";
    public static final String OP_UPDATE_VIEW = "UPDATE_VIEW";
    public static final String OP_LEAVE_GAME = "LEAVE_GAME";
    public static final String OP_CREATE_GAME = "CREATE_GAME";
    public static final String OP_ENTER_GAME = "ENTER_GAME";
    public static final String OP_LIST_GAMES = "LIST_GAMES";
    public static final String OP_PRINT_CARD = "PRINT_CARD";
    public static final String OP_PRINT_COVERED = "PRINT_COVERED";
    public static final String OP_PRINT_SHOWN = "PRINT_SHOWN";
    public static final String OP_PRINT_GOODS = "PRINT_GOODS";
    public static final String OP_PRINT_DASHBOARD = "PRINT_DASHBOARD";
    public static final String OP_GAME_PHASE = "GAME_PHASE";
    public static final String OP_PRINT_DECK = "PRINT_DECK";
    public static final String OP_PRINT_TILE = "PRINT_TILE";
    public static final String OP_SET_CENTRAL_TILE = "SET_CENTRAL_TILE";
    public static final String OP_GET_TILE = "GET_TILE";
    public static final String OP_GET_UNCOVERED = "GET_UNCOVERED";
    public static final String OP_GET_UNCOVERED_LIST = "GET_UNCOVERED_LIST";
    public static final String OP_GET_CARD = "GET_CARD";
    public static final String OP_INDEX = "INDEX";
    public static final String OP_COORDINATE = "COORDINATE";
    public static final String OP_STRING = "STRING";
    public static final String OP_ASK = "ASK";
    public static final String OP_RETURN_TILE = "RETURN_TILE";
    public static final String OP_POSITION_TILE = "POSITION_TILE";
    public static final String OP_ROTATE_GLASS = "ROTATE_GLASS";
    public static final String OP_SET_READY = "SET_READY";
    public static final String OP_SET_IS_DEMO = "SET_IS_DEMO";
    public static final String OP_LOOK_DECK = "LOOK_DECK";
    public static final String OP_LOOK_SHIP = "LOOK_SHIP";
    public static final String OP_SET_NICKNAME = "OP_NICKNAME";
    public static final String OP_SET_GAMEID = "SET_GAMEID";
    public static final String OP_MAP_POSITION = "MAP_POSITION";
    public static final String OP_SET_FLAG_START = "SET_FLAG_START";
    public static final String OP_GET_RESERVED_TILE= "OP_GET_RESERVED_TILE";
    public static final String OP_UPDATE_DA = "UPDATE_DA";
    public static final String OP_ASK_TO = "ASK_TIMEOUT";
    public static final String OP_COORDINATE_TO = "COORDINATE_TIMEOUT";
    public static final String OP_INDEX_TO = "INDEX_TIMEOUT";

    private final String messageType;
    private final String operation;
    private final Object payload;
    private final String requestId;

    /**
     * Constructs a Message with the specified type, operation, payload, and request ID.
     *
     * @param messageType the type of the message (e.g., REQUEST, RESPONSE, etc.)
     * @param operation   the operation to be performed or notified
     * @param payload     the content of the message, if any
     * @param requestId   the ID to correlate request and response
     */
    public Message(String messageType, String operation, Object payload, String requestId) {
        this.messageType = messageType;
        this.operation = operation;
        this.payload = payload;
        this.requestId = requestId;
    }

    /**
     * Creates a new request message with a generated request ID.
     *
     * @param operation the requested operation
     * @param payload   the request content
     * @return a Message instance representing a request
     */
    public static Message request(String operation, Object payload) {
        return new Message(TYPE_REQUEST, operation, payload, UUID.randomUUID().toString());
    }

    /**
     * Creates a new response message referencing a specific request.
     *
     * @param payload   the response content
     * @param requestId the ID of the original request
     * @return a Message instance representing a response
     */
    public static Message response(Object payload, String requestId) {
        return new Message(TYPE_RESPONSE, null, payload, requestId);
    }

    /**
     * Creates an update message with the given operation and payload.
     *
     * @param operation the type of update
     * @param payload   the updated data
     * @return a Message instance representing an update
     */
    public static Message update(String operation, Object payload) {
        return new Message(TYPE_UPDATE, operation, payload, null);
    }

    /**
     * Creates a notification message containing the specified message string.
     *
     * @param message the message to notify
     * @return a Message instance representing a notification
     */
    public static Message notify(String message) {
        return new Message(TYPE_NOTIFICATION, null, message, null);
    }


    /**
     * Creates an error message tied to a specific request ID.
     *
     * @param errorMessage the error description
     * @param requestId    the request ID this error refers to
     * @return a Message instance representing an error response
     */
    public static Message error(String errorMessage, String requestId) {
        return new Message(TYPE_ERROR, null, errorMessage, requestId);
    }

    /**
     * Creates a general error message without referencing a specific request.
     *
     * @param errorMessage the error description
     * @return a Message instance representing a general error
     */
    public static Message error(String errorMessage) {
        return new Message(TYPE_ERROR, null, errorMessage, null);
    }

    /**
     * Gets the message type.
     *
     * @return the message type (e.g., REQUEST, RESPONSE)
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Gets the operation name.
     *
     * @return the operation, or null if not applicable
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets the message payload.
     *
     * @return the payload object, may be null
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Gets the request ID.
     *
     * @return the request ID, may be null
     */
    public String getRequestId() {
        return requestId;
    }

}
