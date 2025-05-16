package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.VirtualViewAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class SaveDeletionTest {
    @TempDir Path tempDir;
    private GameManager gm;
    private Path savesDir;

    @BeforeEach
    void setUp() {
        // punta la directory di salvataggio al tempDir
        savesDir = tempDir.resolve("saves");
        System.setProperty("game.saves.dir", savesDir.toString());
        gm = new GameManager();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("game.saves.dir");
    }

    @Test
    void quitGameRemovesSaveFile() throws Exception {
        // 1) crea e salva la partita
        VirtualViewAdapter stub = new VirtualViewAdapter() {};
        int gameId = gm.createGame(false, stub, "alice", 2);

        // assicuriamoci che il .sav esista
        File[] before = savesDir.toFile().listFiles((d,n)->n.equals("game_"+gameId+".sav"));
        assertNotNull(before);
        assertEquals(1, before.length);

        // 2) fai l’abbandono/quit
        gm.quitGame(gameId, "alice");

        // 3) ora il file NON deve più esistere
        File[] after = savesDir.toFile().listFiles((d,n)->n.equals("game_"+gameId+".sav"));
        assertTrue(after == null || after.length == 0, "Il file di salvataggio deve essere stato cancellato");
    }
}
