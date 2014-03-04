package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public class SPElementaryExchange extends SPExchange {

	private String subCompartment;
	private ElementaryFlowType type;

	public String getSubCompartment() {
		return subCompartment;
	}

	public ElementaryFlowType getType() {
		return type;
	}

	public void setSubCompartment(String subCompartment) {
		this.subCompartment = subCompartment;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}

	/**
	 * Reads an elementary exchange from the given line using the given CSV
	 * separator. Note that the elementary flow type cannot be derived from the
	 * line.
	 */
	public static SPElementaryExchange fromLine(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPElementaryExchange exchange = new SPElementaryExchange();
		exchange.setName(CsvUtils.get(columns, 0));
		exchange.setSubCompartment(CsvUtils.get(columns, 1));
		exchange.setUnit(CsvUtils.get(columns, 2));
		exchange.setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 3)));
		SPUncertaintyDistribution uncertainty = SPUncertaintyDistribution
				.fromCsv(columns, 4);
		exchange.setUncertaintyDistribution(uncertainty);

		// TODO: comment + pedigree
		return exchange;
	}
}
