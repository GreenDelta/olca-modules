package org.openlca.simapro.csv.model;

/**
 * Defines the common fields of a SimaPro exchange.
 */
abstract class SPExchange {

	private String name;
	private String amount;
	private String comment;
	private String unit;
	private SPUncertaintyDistribution uncertaintyDistribution;

	public String getAmount() {
		return amount;
	}

	public String getComment() {
		return comment;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public SPUncertaintyDistribution getUncertaintyDistribution() {
		return uncertaintyDistribution;
	}

	public void setUncertaintyDistribution(
			SPUncertaintyDistribution uncertaintyDistribution) {
		this.uncertaintyDistribution = uncertaintyDistribution;
	}
}
