package fraudscan.main.dto;

public class TransactionRequestDto {
	
	private String timestamp;
    private String from_account;
    private String to_account;
    private int amount;
    private String location;
    
	public TransactionRequestDto(String timestamp, String from_account, String to_account, int amount,
			String location) {
		this.timestamp = timestamp;
		this.from_account = from_account;
		this.to_account = to_account;
		this.amount = amount;
		this.location = location;
	}
	
	public TransactionRequestDto(){
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getFrom_account() {
		return from_account;
	}

	public void setFrom_account(String from_account) {
		this.from_account = from_account;
	}

	public String getTo_account() {
		return to_account;
	}

	public void setTo_account(String to_account) {
		this.to_account = to_account;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	} 

}
