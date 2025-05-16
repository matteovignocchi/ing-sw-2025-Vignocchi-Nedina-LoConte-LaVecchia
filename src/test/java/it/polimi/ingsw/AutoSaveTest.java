package it.polimi.ingsw;

import it.polimi.ingsw.galaxytrucker.Server.GameManager;
import it.polimi.ingsw.galaxytrucker.Server.VirtualViewAdapter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AutoSaveTest {
    @TempDir Path tempDir;
    private GameManager gm;
    private Path savesDir;

    @BeforeEach
    void setUp() {
        savesDir = tempDir.resolve("saves");
        System.setProperty("game.saves.dir", savesDir.toString());
        gm = new GameManager();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("game.saves.dir");
    }

    @Test
    void periodicSaveCreatesFile() throws Exception {
        VirtualViewAdapter stub = new VirtualViewAdapter() {};
        int id = gm.createGame(false, stub, "alice", 2);

        // attendi un po' più di un periodo (1s nel test anziché 1min)
        // per il test puoi aver configurato il scheduler su 1s anziché 1min:
        TimeUnit.SECONDS.sleep(2);

        File[] files = savesDir.toFile().listFiles((d,n)->n.equals("game_"+id+".sav"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Il salvataggio periodico deve aver creato il .sav");
    }
}
