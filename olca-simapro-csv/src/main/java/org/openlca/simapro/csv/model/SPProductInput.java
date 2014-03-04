package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public class SPProductInput extends SPExchange {

	private ProductFlowType type;

	public ProductFlowType getType() {
		return type;
	}

	public void setType(ProductFlowType type) {
		this.type = type;
	}

	public static SPProductInput fromCsv(String line, String separator) {
		String[] columns = CsvUtils.split(line, separator);
		SPProductInput input = new SPProductInput();
		input.setName(CsvUtils.get(columns, 0));
		input.setUnit(CsvUtils.get(columns, 1));
		input.setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 2)));
		SPUncertainty uncertainty = SPUncertainty.fromCsv(columns, 3);
		input.setUncertaintyDistribution(uncertainty);
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 7));
		input.setComment(comment);
		input.setPedigreeUncertainty(CsvUtils.getPedigreeUncertainty(comment));
		return input;
	}

	public String toCsv(String separator) {
		String[] line = new String[8];
		line[0] = getName();
		line[1] = getUnit();
		line[2] = getAmount();
		if (getUncertaintyDistribution() != null)
			getUncertaintyDistribution().toCsv(line, 3);
		else
			SPUncertainty.undefinedToCsv(line, 3);
		String comment = getComment();
		if (comment == null)
			comment = "";
		String pedigree = getPedigreeUncertainty();
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[7] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(separator).join(line);
	}

}
