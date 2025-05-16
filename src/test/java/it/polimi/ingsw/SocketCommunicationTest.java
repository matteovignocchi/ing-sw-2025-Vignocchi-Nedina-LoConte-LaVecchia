package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Client.Message;
import it.polimi.ingsw.galaxytrucker.Model.Tile.Tile;
import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.ServerSocketMain;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SocketCommunicationTest {

    /**
     * Legge in loop finché non incontra un TYPE_RESPONSE.
     */
    private Message nextResponse(ObjectInputStream in) throws Exception {
        Message msg;
        do {
            msg = (Message) in.readObject();
        } while (!Message.TYPE_RESPONSE.equals(msg.getMessageType()));
        return msg;
    }

    @Test
    void loginCreateListJoinAndGetTileOverSocket() throws Exception {
        GameManager gm = new GameManager();
        int port = 34567;
        ServerSocketMain server = new ServerSocketMain(gm, port);
        Thread srvThread = new Thread(server);
        srvThread.start();

        try (
                Socket aliceSock = new Socket("localhost", port);
                ObjectOutputStream aliceOut = new ObjectOutputStream(aliceSock.getOutputStream());
                ObjectInputStream  aliceIn  = new ObjectInputStream(aliceSock.getInputStream())
        ) {
            aliceOut.flush();

            // ——— LOGIN alice ———
            aliceOut.writeObject(Message.request(Message.OP_LOGIN, "alice"));
            aliceOut.flush();
            Message loginResp = nextResponse(aliceIn);
            assertEquals(Message.TYPE_RESPONSE, loginResp.getMessageType());
            assertEquals("OK", loginResp.getPayload());

            // ——— CREATE GAME ———
            List<Object> createPayload = List.of(false, "alice", 2);
            aliceOut.writeObject(Message.request(Message.OP_CREATE_GAME, createPayload));
            aliceOut.flush();
            Message createResp = nextResponse(aliceIn);
            assertEquals(Message.TYPE_RESPONSE, createResp.getMessageType());
            int gameId = (Integer) createResp.getPayload();
            assertTrue(gameId > 0);

            // ——— LIST GAMES ———
            aliceOut.writeObject(Message.request(Message.OP_LIST_GAMES, null));
            aliceOut.flush();
            Message listResp = nextResponse(aliceIn);
            assertEquals(Message.TYPE_RESPONSE, listResp.getMessageType());
            @SuppressWarnings("unchecked")
            Map<Integer,int[]> games = (Map<Integer,int[]>) listResp.getPayload();
            assertTrue(games.containsKey(gameId));
            int[] info = games.get(gameId);
            assertEquals(1, info[0]);
            assertEquals(2, info[1]);

            // ——— JOIN GAME bob ———
            try (
                    Socket bobSock = new Socket("localhost", port);
                    ObjectOutputStream bobOut = new ObjectOutputStream(bobSock.getOutputStream());
                    ObjectInputStream  bobIn  = new ObjectInputStream(bobSock.getInputStream())
            ) {
                bobOut.flush();

                // bob si logga
                bobOut.writeObject(Message.request(Message.OP_LOGIN, "bob"));
                bobOut.flush();
                nextResponse(bobIn); // ignoro eventuali UPDATE

                // bob entra nella game di alice
                List<Object> joinPayload = List.of(gameId, "bob");
                bobOut.writeObject(Message.request(Message.OP_ENTER_GAME, joinPayload));
                bobOut.flush();
                Message joinResp = nextResponse(bobIn);
                assertEquals(Message.TYPE_RESPONSE, joinResp.getMessageType());
                assertEquals("OK", joinResp.getPayload());

                // verifica che nel GameManager ci siano 2 connessi
                int[] afterJoin = gm.listActiveGames().get(gameId);
                assertEquals(2, afterJoin[0]);
                assertEquals(2, afterJoin[1]);
            }

            // ——— GET_TILE da alice ———
            aliceOut.writeObject(Message.request(
                    Message.OP_GET_TILE,
                    List.of(gameId, "alice")
            ));
            aliceOut.flush();
            Message tileResp = nextResponse(aliceIn);
            assertEquals(Message.TYPE_RESPONSE, tileResp.getMessageType());
            assertTrue(tileResp.getPayload() instanceof Tile,
                    "Il payload deve essere un Tile");

        } finally {
            srvThread.interrupt();
            srvThread.join();
        }
    }
}
