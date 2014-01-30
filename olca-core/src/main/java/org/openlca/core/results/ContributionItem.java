package org.openlca.core.results;

public class ContributionItem<T> {

	private T item;
	private double amount;
	private double share;
	private boolean rest = false;

	public T getItem() {
		return item;
	}

	public void setItem(T item) {
		this.item = item;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getShare() {
		return share;
	}

	public void setShare(double share) {
		this.share = share;
	}

	public boolean isRest() {
		return rest;
	}

	public void setRest(boolean rest) {
		this.rest = rest;
	}

}
