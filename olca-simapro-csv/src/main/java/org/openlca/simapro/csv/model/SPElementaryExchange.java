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
	public static SPElementaryExchange fromCsv(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPElementaryExchange exchange = new SPElementaryExchange();
		exchange.setName(CsvUtils.get(columns, 0));
		exchange.setSubCompartment(CsvUtils.get(columns, 1));
		exchange.setUnit(CsvUtils.get(columns, 2));
		exchange.setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 3)));
		SPUncertainty uncertainty = SPUncertainty.fromCsv(columns, 4);
		exchange.setUncertaintyDistribution(uncertainty);
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 8));
		exchange.setComment(comment);
		exchange.setPedigreeUncertainty(CsvUtils
				.getPedigreeUncertainty(comment));
		return exchange;
	}

	public String toCsv(String separator) {
		String[] line = new String[9];
		line[0] = getName();
		line[1] = subCompartment;
		line[2] = getUnit();
		line[3] = getAmount();
		if (getUncertaintyDistribution() != null)
			getUncertaintyDistribution().toCsv(line, 4);
		else
			SPUncertainty.undefinedToCsv(line, 4);
		String comment = getComment();
		if (comment == null)
			comment = "";
		String pedigree = getPedigreeUncertainty();
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[8] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(separator).join(line);
	}
}
