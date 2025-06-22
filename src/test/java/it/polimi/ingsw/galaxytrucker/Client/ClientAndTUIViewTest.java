//package it.polimi.ingsw.galaxytrucker.Client;
////import it.polimi.ingsw.galaxytrucker.Client.ClientMain;
//import it.polimi.ingsw.galaxytrucker.View.TUIView;
//import org.junit.jupiter.api.*;
//
//import java.io.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class ClientAndTUIViewTest {
//
//    private final PrintStream originalOut = System.out;
//    private final InputStream originalIn = System.in;
//    private ByteArrayOutputStream outContent;
//
//    @BeforeEach
//    public void setUpStreams() {
//        outContent = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outContent));
//    }
//
//    @AfterEach
//    public void restoreStreams() {
//        System.setOut(originalOut);
//        System.setIn(originalIn);
//    }
//
//    @Test
//    void testTUIViewAskString() {
//        String input = "TestString\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        TUIView tui = new TUIView();
//        String result = tui.askString();
//
//        assertEquals("TestString", result);
//    }
//
//    @Test
//    void testTUIViewAskIndex() {
//        String input = "2\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        TUIView tui = new TUIView();
//        int index = tui.askIndex();
//
//        assertEquals(1, index);
//    }
//
//    @Test
//    void testTUIViewAskCoordinate() {
//        String input = "5\n4\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        TUIView tui = new TUIView();
//        int[] coords = tui.askCoordinate();
//
//        assertArrayEquals(new int[]{0, 0}, coords);
//    }

//    @Test
//    void testClientMainSimulatedInput() {
//        String input = "1\n1\nFakeUser\n3\n"; // RMI, TUI, Username, Logout
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        SecurityManager originalSecurityManager = System.getSecurityManager();
//        System.setSecurityManager(new NoExitSecurityManager());
//
//        try {
//            assertThrows(SecurityException.class, () -> {
//                ClientMain.main(new String[0]);
//            });
//        } finally {
//            System.setSecurityManager(originalSecurityManager);
//        }
//    }
//
//    static class NoExitSecurityManager extends SecurityManager {
//        @Override
//        public void checkPermission(java.security.Permission perm) {
//            // allow everything
//        }
//
//        @Override
//        public void checkExit(int status) {
//            super.checkExit(status);
//            throw new SecurityException("System.exit called");
//        }
//    }
//}

