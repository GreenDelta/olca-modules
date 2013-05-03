package org.openlca.core.model.results;

import org.openlca.core.model.Indexable;

/** Generic container for results of a contribution analysis. */
public class Contribution<T extends Indexable> {

	private T item;
	private double amount;
	private double share;

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

}
