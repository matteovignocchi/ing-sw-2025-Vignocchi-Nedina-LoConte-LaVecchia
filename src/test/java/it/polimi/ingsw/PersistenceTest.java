package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Client.VirtualView;
import it.polimi.ingsw.galaxytrucker.Server.VirtualViewAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceTest {
    @TempDir
    Path tempDir;
    GameManager gm;

    @BeforeEach
    void setUp() {
        System.setProperty("game.saves.dir", tempDir.resolve("saves").toString());
        gm = new GameManager();
    }
    @AfterEach
    void tearDown() {
        System.clearProperty("game.saves.dir");
    }

    @Test
    void saveStateCreatesSavFile() throws Exception {
        VirtualView stub = new VirtualViewAdapter() {};
        int id = gm.createGame(false, stub, "alice", 2);

        // ——— DEBUG: stampa a schermo tutti i .sav
        File dir = tempDir.resolve("saves").toFile();
        System.out.println(">>> saves dir contents:");
        for (File f : dir.listFiles()) {
            System.out.println("    " + f.getName());
        }
        System.out.println(">>> end of saves dir");

        File[] files = dir.listFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);
        assertEquals("game_" + id + ".sav", files[0].getName());
    }
    @Test
    void loadSavedGamesRestoresState() throws Exception {
        VirtualView stub = new VirtualViewAdapter() {};
        int id = gm.createGame(false, stub, "bob", 2);
        // sempre senza setReady

        GameManager gm2 = new GameManager();
        assertTrue(gm2.listActiveGames().containsKey(id), "Loaded game ID");
    }
}

