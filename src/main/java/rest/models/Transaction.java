package rest.models;

import java.util.Date;

public class Transaction {

	private double amount;
	private long timestamp;
	
	public Transaction() {}
	
	public Transaction(double amount) {
		this(amount, new Date().getTime());
	}
	
	public Transaction(double amount, long timestamp) {
		setAmount(amount);
		setTimestamp(timestamp);
	}
	
	
	
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
