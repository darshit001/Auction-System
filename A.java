import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OnlineAuctionSystem extends Application {

    private List<Player> availablePlayers = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private ObservableList<Player> playerObservableList;
    private ObservableList<Team> teamObservableList;
    private BufferedWriter historyLogWriter;
    private BufferedWriter playerDetailsWriter;

    public OnlineAuctionSystem() {
        playerObservableList = FXCollections.observableArrayList(availablePlayers);
        teamObservableList = FXCollections.observableArrayList(teams);
        try {
            historyLogWriter = new BufferedWriter(new FileWriter("history_log.txt", true));
            playerDetailsWriter = new BufferedWriter(new FileWriter("player_details.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Main Layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // Player Management Tab
        Tab playerTab = new Tab("Players");
        GridPane playerGrid = createPlayerGrid();
        playerTab.setContent(playerGrid);

        // Team Management Tab
        Tab teamTab = new Tab("Teams");
        GridPane teamGrid = createTeamGrid();
        teamTab.setContent(teamGrid);

        // Auction Tab
        Tab auctionTab = new Tab("Auction");
        HBox auctionBox = createAuctionBox();
        auctionTab.setContent(auctionBox);

        // Stats Tab
        Tab statsTab = new Tab("Stats");
        TextArea statsTextArea = createStatsTextArea();
        statsTab.setContent(statsTextArea);

        // Tab Pane
        TabPane tabPane = new TabPane(playerTab, teamTab, auctionTab, statsTab);
        mainLayout.getChildren().add(tabPane);

        // Scene
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Online Auction System");
        primaryStage.show();
    }

    private GridPane createPlayerGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 0, 10, 0));

        Label nameLabel = new Label("Name:");
        TextField nameInput = new TextField();
        gridPane.addRow(0, nameLabel, nameInput);

        Label bidLabel = new Label("Bid Amount:");
        TextField bidInput = new TextField();
        // Set number formatter to restrict input to numbers
        bidInput.setTextFormatter(new NumberStringConverter());
        gridPane.addRow(1, bidLabel, bidInput);

        Label roleLabel = new Label("Role:");
        ChoiceBox<String> roleChoiceBox = new ChoiceBox<>();
        roleChoiceBox.getItems().addAll("Bidder", "Observer");
        gridPane.addRow(2, roleLabel, roleChoiceBox);

        Button addPlayerButton = new Button("Add Player");
        addPlayerButton.setOnAction(e -> {
            try {
                String name = nameInput.getText();
                double bidAmount = Double.parseDouble(bidInput.getText());
                String role = roleChoiceBox.getValue();
                addPlayer(name, bidAmount, role);
                playerObservableList.add(new Player(name, bidAmount, role));
                clearPlayerInput(nameInput, bidInput, roleChoiceBox);
                updateStats();
            } catch (NumberFormatException ex) {
                displayAlert(Alert.AlertType.ERROR, "Invalid Input", "Bid Amount must be a number.");
            }
        });
        gridPane.addRow(3, addPlayerButton);
