package rest.services;

import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import rest.models.Transaction;
import rest.models.TransactionsStatistics;

public class TransactionsStatisticsService  {

	private long timeLimit;
	private long updatePeriod;
	
	private PriorityBlockingQueue<Transaction> transactions;
	private TransactionsStatistics statistics;
	private Timer timer;
	
	/**
	 * Creates a TransactionsStatisticsService with given
	 * time limit and update period.
	 * <p>
	 * It keeps transactions statistics within the given <i>timeLimit</i>.
	 * <p>
	 * Statistics are updated depending on <i>updatePeriod</i>.
	 * 
	 * @param timeLimit - the time limit, which if exceeded by a timestamp (current moment - timestamp),
	 * leads to removing the transaction from the statistics
	 * @param updatePeriod - time period for updating statistics <b>in ms</b>
	 */
	public TransactionsStatisticsService(long timeLimit, long updatePeriod) {
		this.setTimeLimit(timeLimit);
		this.setUpdatePeriod(updatePeriod);
		
		this.setStatistics(new TransactionsStatistics());
		
		//capacity big enough to allow 1 million transactions per second
		int capacity = (int) timeLimit * 1000;
		this.transactions = new PriorityBlockingQueue<Transaction>(capacity, new Comparator<Transaction>() {

			@Override
			public int compare(Transaction o1, Transaction o2) {
				if(o1.getTimestamp() < o2.getTimestamp())
					return -1;
				else if(o1.getTimestamp() > o2.getTimestamp())
					return 1;
				else return 0;
			}
			
		});
	}
	
	/**
	 * Runs this service, to allow it to update statistics regularly.
	 */
	public void execute() {
		//clean up old timer
		if(this.timer != null)
			this.timer.cancel();
		
		this.timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				update();
			}
		}, 0, this.updatePeriod);
	}
	
	/**
	 * Stops the service.
	 */
	public void stop() {
		this.timer.cancel();
		this.transactions.clear();
	}
	
	/**
	 * Add transaction in the statistics.
	 * 
	 * @param tr Transaction
	 * @return true, if and only if the transaction is added to statistics, false otherwise
	 */
	public synchronized boolean add(Transaction tr) {
		if(isTransactionValid(tr)) {
			this.transactions.put(tr);
			
			/**
			 * IF complexity (O(1)) is <b>NOT</b> necessary for this method (endpoint),
			 * then update() has to be called to ensure correct statistics.
			 */
//			update();
			
			return true;
		}
		return false;
	}
	
	
	
	private boolean isTransactionValid(Transaction tr) {
		if(tr == null) return false;
		
		long now = new Date().getTime();
		return (now - tr.getTimestamp()) < this.timeLimit && tr.getTimestamp() <= now;
	}
	
	/**
	 * Removes old transaction and updates statistics.
	 */
	private synchronized void update() {
		//remove old transactions
		while(!isTransactionValid(this.transactions.peek()) && this.transactions.peek() != null) {
			this.transactions.remove();
		}
		
		//update statistics
		double sum = 0d;
		double max = 0d;
		double min = Double.MAX_VALUE;
		
		for(Transaction tr : this.transactions) {
			double amount = tr.getAmount();
			
			sum += amount;
			max = Math.max(max, amount);
			min = Math.min(min, amount);
		}
		
		this.statistics.update(sum, max, min, this.transactions.size());
	}
	
	

	/**
	 * Return TransactionsStatistics.
	 * 
	 * @return TransactionsStatistics
	 */
	public synchronized TransactionsStatistics getStatistics() {
		//no updates to keep constant complexity
		return this.statistics;
	}
	private void setStatistics(TransactionsStatistics statistics) {
		this.statistics = statistics;
	}
	public long getTimeLimit() {
		return timeLimit;
	}
	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}
	public long getUpdatePeriod() {
		return updatePeriod;
	}
	/**
	 * Setting new updatePeriod when this service has been already executed,
	 * requires calling of stop() and execute() methods, in oder to apply the new updatePeriod.
	 * 
	 * @param updatePeriod - in ms
	 */
	public void setUpdatePeriod(long updatePeriod) {
		this.updatePeriod = updatePeriod;
	}
}
