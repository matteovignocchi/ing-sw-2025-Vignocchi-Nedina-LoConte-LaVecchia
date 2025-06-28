
package it.polimi.ingsw.galaxytrucker.View.GUI.Controllers;
import it.polimi.ingsw.galaxytrucker.View.GUI.GUIView;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

/**
 * Controller for the GUI scene that manages the player's interaction with goods.
 * Provides UI to select actions on goods (add, rearrange, trash), navigate among goods,
 * display the current good visually, and close the window after selection.
 * @author Matteo Vignocchi
 * @author Oleg Nedina
 */
public class PrintListOfGoodController extends GUIController {

    @FXML private Pane goodPane;
    @FXML private Button addGood, rearranges, trash, leftBtn, rightBtn;
    private int currentGoodIndex = 0;
    private List<String> loadedGoods;
    private Stage ownStage;

    /**
     * Prepares the UI for selecting an action on goods.
     * Shows action buttons (Add, Rearranges, Trash) and hides navigation buttons.
     * Sets event handlers to set the buffered index in the GUI view and close the window on selection.
     * @param gui the GUIView instance for buffering the user choice
     */
    public void setupForActionSelection(GUIView gui) {
        goodPane.setVisible(false);
        addGood.setVisible(true);
        rearranges.setVisible(true);
        trash.setVisible(true);
        leftBtn.setVisible(false);
        rightBtn.setVisible(false);

        addGood.setOnAction(e -> {
            gui.setBufferedIndex(0);
            closeWindow();
        });
        rearranges.setOnAction(e -> {
            gui.setBufferedIndex(1);
            closeWindow();
        });
        trash.setOnAction(e -> {
            gui.setBufferedIndex(2);
            closeWindow();
        });
    }

    /**
     * Prepares the UI for selecting a specific good from the list.
     * Shows the good display pane and navigation buttons,
     * hides action selection buttons.
     */
    public void setupForGoodsIndexSelection() {
        goodPane.setVisible(true);
        addGood.setVisible(false);
        rearranges.setVisible(false);
        trash.setVisible(false);
        leftBtn.setVisible(true);
        rightBtn.setVisible(true);
    }

    /**
     * Loads the list of goods to display and initializes the selection index.
     * Displays the first good visually.
     * @param goods list of goods as color strings
     * @param guiView the GUIView instance to interact with
     */
    public void loadGoods(List<String> goods, GUIView guiView) {
        this.loadedGoods = goods;
        this.currentGoodIndex = 0;
        showGood(guiView);
    }

    /**
     * Displays the currently selected good with its image in the pane.
     * Sets up the image view with proper styling and mouse click animation
     * that notifies the GUI view of the selection and closes the window.
     * @param gui the GUIView instance to notify upon selection
     */
    public void showGood(GUIView gui) {
        goodPane.getChildren().clear();

        if (loadedGoods == null || loadedGoods.isEmpty()) return;

        String color = loadedGoods.get(currentGoodIndex).toUpperCase();
        String path = switch (color) {
            case "BLUE" -> "/images/BlueGood.png";
            case "RED" -> "/images/RedGood.png";
            case "GREEN" -> "/images/GreenGood.png";
            case "YELLOW" -> "/images/YellowGood.png";
            default -> "/images/placeholder.png";
        };

        ImageView iv = new ImageView(new Image(getClass().getResource(path).toExternalForm()));

        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setCursor(Cursor.HAND); // 👈 mostra la "manina"

        iv.fitWidthProperty().bind(goodPane.widthProperty());
        iv.fitHeightProperty().bind(goodPane.heightProperty());

        iv.setOnMouseClicked(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), iv);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(0.85);
            st.setToY(0.85);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.setOnFinished(ev -> {
                if (gui != null) gui.setBufferedIndex(currentGoodIndex);
                closeWindow();
            });
            st.play();
        });

        goodPane.getChildren().add(iv);
    }

    /**
     * Configures the left and right navigation buttons to cycle through the goods list.
     * Updates the displayed good accordingly and notifies the GUI view on selection.
     * @param gui the GUIView instance to notify upon selection
     */
    public void configureNavigation(GUIView gui) {
        leftBtn.setVisible(true);
        rightBtn.setVisible(true);

        leftBtn.setOnAction(e -> {
            currentGoodIndex = (currentGoodIndex - 1 + loadedGoods.size()) % loadedGoods.size();
            showGood(gui);
        });

        rightBtn.setOnAction(e -> {
            currentGoodIndex = (currentGoodIndex + 1) % loadedGoods.size();
            showGood(gui);
        });
    }

    /**
     * Closes the current window if the stage is set.
     * Logs an error if the stage is not set.
     */
    private void closeWindow() {
        if (ownStage != null) {
            ownStage.close();
        } else {
            System.err.println("Stage non settato! Impossibile chiudere finestra.");
        }
    }

    /**
     * Sets the JavaFX Stage that owns this controller's window.
     * @param stage the Stage instance
     */
    public void setStage(Stage stage) {
        this.ownStage = stage;
    }

    /**
     * Empty override of postInitialize; no additional setup required after scene load.
     */
    @Override
    public void postInitialize() {}
}