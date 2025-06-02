package fraudscan.main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "from_account", nullable = false)
	private String fromAccount;

	@Column(name = "to_account", nullable = false)
	private String toAccount;

	@Column(nullable = false)
	private double amount;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	private String location;

	@Column(name = "predicted_fraud")
	private boolean predictedFraud;

	public Transaction(Long id, String fromAccount, String toAccount, double amount, LocalDateTime timestamp,
			String location, boolean predictedFraud) {
		this.id = id;
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.amount = amount;
		this.timestamp = timestamp;
		this.location = location;
		this.predictedFraud = predictedFraud;
	}
	
	public Transaction(String fromAccount, String toAccount, double amount, LocalDateTime timestamp,
			String location, boolean predictedFraud) {
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.amount = amount;
		this.timestamp = timestamp;
		this.location = location;
		this.predictedFraud = predictedFraud;
	}
	
	public Transaction() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isPredictedFraud() {
		return predictedFraud;
	}

	public void setPredictedFraud(boolean predictedFraud) {
		this.predictedFraud = predictedFraud;
	}
	
	
	
	

}
