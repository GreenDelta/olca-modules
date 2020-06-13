package org.openlca.simapro.csv.model;

/**
 * Defines the common fields of a SimaPro exchange.
 */
public abstract class ExchangeRow implements IDataRow {

	public String name;
	public String amount;
	public String comment;
	public String unit;
	public Uncertainty uncertaintyDistribution;
	public String pedigreeUncertainty;
}
