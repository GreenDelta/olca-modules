package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.model.IDataRow;
import org.openlca.simapro.csv.model.Uncertainty;

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
