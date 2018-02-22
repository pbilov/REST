package rest.models;

/**
 * TransactionsStatistics container class.
 */
public class TransactionsStatistics {

	private double sum = 0d;
	private double avg = 0d;
	private double max = 0d;
	private double min = 0d;
	private long count = 0l;

	/**
	 * Updates statistics.
	 */
	public void update(double sum, double max, double min, long count) {
		this.setSum(sum);
		this.setMax(max);
		this.setMin(min);
		this.setCount(count);
		
		if(this.count > 0) 
			this.setAvg(this.sum / (double) this.count);
		else this.setAvg(0d);
	}
	
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}
	public double getAvg() {
		return avg;
	}
	public void setAvg(double avg) {
		this.avg = avg;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
}
