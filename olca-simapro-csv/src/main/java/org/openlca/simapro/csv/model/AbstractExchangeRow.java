package org.openlca.simapro.csv.model;

/**
 * Defines the common fields of a SimaPro exchange.
 */
public abstract class AbstractExchangeRow implements IDataRow {

	private String name;
	private String amount;
	private String comment;
	private String unit;
	private Uncertainty uncertaintyDistribution;
	private String pedigreeUncertainty;

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

	public Uncertainty getUncertaintyDistribution() {
		return uncertaintyDistribution;
	}

	public void setUncertaintyDistribution(Uncertainty uncertaintyDistribution) {
		this.uncertaintyDistribution = uncertaintyDistribution;
	}

	public String getPedigreeUncertainty() {
		return pedigreeUncertainty;
	}

	public void setPedigreeUncertainty(String pedigreeUncertainty) {
		this.pedigreeUncertainty = pedigreeUncertainty;
	}
}
