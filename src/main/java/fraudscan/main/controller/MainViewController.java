package fraudscan.main.controller;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import fraudscan.main.client.AIClient;
import fraudscan.main.dto.TransactionRequestDto;
import fraudscan.main.model.Transaction;
import fraudscan.main.repository.TransactionRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

@Component
public class MainViewController {

    @FXML private Label lblMainTitle;
    @FXML private Label lblTransactionsTitle;
    @FXML private Label lblFraudTransactionsTitle;

    @FXML private Button btnStartSimulation;
    @FXML private Button btnDatabaseDelete;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colFrom;
    @FXML private TableColumn<Transaction, String> colTo;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colAmount;

    @FXML private TableView<String> fraudTables;
    @FXML private TableColumn<String, String> colFraudAccounts;

    private boolean simulationRunning = false;
    private ScheduledExecutorService executor;

    private String lastUsedFromAccount = null;
    private int repeatFromAccountCount = 0;

    private final TransactionRepository transactionRepository;
    private final AIClient aiClient;

    private final Set<String> suspiciousAccounts = new HashSet<>();

    public MainViewController(TransactionRepository transactionRepository, AIClient aiClient) {
        this.transactionRepository = transactionRepository;
        this.aiClient = aiClient;
    }

    @FXML
    public void initialize() {
        colFrom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFromAccount()));
        colTo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getToAccount()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))));
        colAmount.setCellValueFactory(data -> {
            int rawAmount = (int) data.getValue().getAmount();
            NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("hu", "HU"));
            String formatted = nf.format(rawAmount) + " Ft";
            return new SimpleStringProperty(formatted);
        });

        colFrom.setStyle("-fx-alignment: CENTER;");
        colTo.setStyle("-fx-alignment: CENTER;");
        colDate.setStyle("-fx-alignment: CENTER;");
        colAmount.setStyle("-fx-alignment: CENTER;");

        transactionTable.setItems(FXCollections.observableArrayList());
        fraudTables.setItems(FXCollections.observableArrayList());
        colFraudAccounts.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        colFraudAccounts.setStyle("-fx-alignment: CENTER;");

        btnStartSimulation.setOnAction(e -> toggleSimulation());

        transactionTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && transactionTable.getSelectionModel().getSelectedItem() != null) {
                Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
                showTransactionDetails(selected);
            }
        });
    }

    private Transaction generateRandomTransaction() {
        Random rand = new Random();

        String fromAcc;
        if (repeatFromAccountCount > 0 && lastUsedFromAccount != null) {
            fromAcc = lastUsedFromAccount;
            repeatFromAccountCount--;
        } else {
            fromAcc = String.format("%08d", rand.nextInt(100_000_000));
            lastUsedFromAccount = fromAcc;

            if (rand.nextInt(100) < 5) {
                repeatFromAccountCount = 2 + rand.nextInt(3);
            }
        }

        String toAcc = String.format("%08d", rand.nextInt(100_000_000));

        int chance = rand.nextInt(100);
        int amount;

        if (chance < 50) {
            amount = 100_000 + rand.nextInt(100_001);
        } else if (chance < 70) {
            amount = 10_000 + rand.nextInt(90_000);
        } else if (chance < 80) {
            amount = 200_001 + rand.nextInt(100_000);
        } else if (chance < 90) {
            amount = 300_001 + rand.nextInt(100_000);
        } else if (chance < 95) {
            amount = 400_001 + rand.nextInt(100_000);
        } else {
            amount = 1_000_001 + rand.nextInt(999_000_000);
        }

        LocalDateTime time = LocalDateTime.now().withSecond(0).withNano(0);
        String[] locations = {"HU", "DE", "SK", "RO", "PL"};
        String location = locations[rand.nextInt(locations.length)];

        return new Transaction(fromAcc, toAcc, amount, time, location, false);
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
                            Transaction t = generateRandomTransaction();

                            // DTO küldés
                            TransactionRequestDto dto = new TransactionRequestDto(
                                t.getTimestamp().toString(),
                                t.getFromAccount(),
                                t.getToAccount(),
                                (int) t.getAmount(),
                                t.getLocation()
                            );

                            boolean isFraud = aiClient.isFraudulent(dto);
                            t.setPredictedFraud(isFraud);

                            transactionTable.getItems().add(t);
                            transactionRepository.save(t);

                            if (isFraud) {
                                String fromAcc = t.getFromAccount();
                                if (!suspiciousAccounts.contains(fromAcc)) {
                                    suspiciousAccounts.add(fromAcc);
                                    fraudTables.getItems().add(fromAcc);
                                }
                            }
                        }
                    });

                    int delay = 300 + new Random().nextInt(3200);
                    executor.schedule(this, delay, TimeUnit.MILLISECONDS);
                }
            };

            executor.schedule(task, 0, TimeUnit.MILLISECONDS);
        }
    }

    private void showTransactionDetails(Transaction transaction) {
        StringBuilder details = new StringBuilder();
        details.append("📋 Tranzakció részletei:\n\n");
        details.append("➡ Feladó fiók: ").append(transaction.getFromAccount()).append("\n");
        details.append("⬅ Címzett fiók: ").append(transaction.getToAccount()).append("\n");
        details.append("💰 Összeg: ").append(transaction.getAmount()).append(" Ft\n");
        details.append("🕒 Időpont: ").append(
                transaction.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
        ).append("\n");
        details.append("🌍 Helyszín: ").append(transaction.getLocation()).append("\n");
        details.append("🚨 Gyanús: ").append(transaction.isPredictedFraud() ? "Igen" : "Nem");

        Label contentLabel = new Label(details.toString());
        contentLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI';");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tranzakció részletei");
        alert.setHeaderText("Kiválasztott utalás");
        alert.getDialogPane().setContent(contentLabel);
        alert.showAndWait();
    }

    public void clearDatabase() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Adatbázis Törlés");
        confirm.setHeaderText("Biztosan törlöd az összes tranzakciót?");
        confirm.setContentText("Ez a művelet nem visszavonható!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            transactionRepository.deleteAll();
            transactionTable.getItems().clear();
            fraudTables.getItems().clear();
            suspiciousAccounts.clear();
            System.out.println("✅ Adatbázis és UI kiürítve.");
        }
    }
}


