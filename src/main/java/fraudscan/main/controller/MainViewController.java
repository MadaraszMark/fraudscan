package fraudscan.main.controller;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import fraudscan.main.model.Transaction;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

@Component
public class MainViewController {

    @FXML private Label lblMainTitle;
    @FXML private Label lblTransactionsTitle;
    @FXML private Label lblFraudTransactionsTitle;

    @FXML private Button btnStartSimulation;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colFrom;
    @FXML private TableColumn<Transaction, String> colTo;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colAmount;

    @FXML private TableView<String> fraudTables;
    @FXML private TableColumn<String, String> colFraudAccounts;

    private boolean simulationRunning = false;
    private ScheduledExecutorService executor;

    @FXML
    public void initialize() {
        // --- Cellákhoz oszlop-hozzárendelés ---
        colFrom.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getFromAccount()));
        colTo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getToAccount()));
        colDate.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))));
        colAmount.setCellValueFactory(data -> {
            double rawAmount = data.getValue().getAmount();
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("hu", "HU"));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            String formatted = nf.format(rawAmount); // pl. 4 784,00
            return new SimpleStringProperty(formatted);
        });

        // --- Igazítások ---
        colFrom.setStyle("-fx-alignment: CENTER-LEFT;");
        colTo.setStyle("-fx-alignment: CENTER-LEFT;");
        colDate.setStyle("-fx-alignment: CENTER;");
        colAmount.setStyle("-fx-alignment: CENTER-RIGHT;");

        // --- Kezdő állapot ---
        transactionTable.setItems(FXCollections.observableArrayList());
        fraudTables.setItems(FXCollections.observableArrayList());

        btnStartSimulation.setOnAction(e -> toggleSimulation());
    }

    private Transaction generateRandomTransaction() {
        Random rand = new Random();

        String fromAcc = String.valueOf(300000000 + rand.nextInt(999999999));
        String toAcc = String.valueOf(200000000 + rand.nextInt(999999999));

        double amount = 1000 + rand.nextDouble() * 15000;
        amount = Math.round(amount * 100.0) / 100.0;

        LocalDateTime time = LocalDateTime.now().withSecond(0).withNano(0);
        String[] locations = {"HU", "DE", "SK", "RO", "PL"};
        String location = locations[rand.nextInt(locations.length)];
        boolean isFraud = rand.nextInt(10) == 0;

        return new Transaction(fromAcc, toAcc, amount, time, location, isFraud);
    }

    private void toggleSimulation() {
        if (simulationRunning) {
            simulationRunning = false;
            executor.shutdownNow();
            btnStartSimulation.setText("Start Szimuláció");
        } else {
            simulationRunning = true;
            executor = Executors.newSingleThreadScheduledExecutor();
            btnStartSimulation.setText("Stop Szimuláció");

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    if (!simulationRunning) return;

                    Platform.runLater(() -> {
                        int transactionsNow = new Random().nextInt(3) + 1;
                        for (int i = 0; i < transactionsNow; i++) {
                            transactionTable.getItems().add(generateRandomTransaction());
                        }
                    });

                    int delay = 300 + new Random().nextInt(3200);
                    executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                }
            };

            executor.schedule(task, 0, TimeUnit.MILLISECONDS);
        }
    }
}
