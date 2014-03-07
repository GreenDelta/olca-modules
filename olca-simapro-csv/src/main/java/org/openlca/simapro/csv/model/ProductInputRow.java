package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.enums.ProductFlowType;

public class ProductInputRow extends ExchangeRow {

	private ProductFlowType type;

	public ProductFlowType getType() {
		return type;
	}

	public void setType(ProductFlowType type) {
		this.type = type;
	}

	public static ProductInputRow fromCsv(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		ProductInputRow input = new ProductInputRow();
		input.setName(CsvUtils.get(columns, 0));
		input.setUnit(CsvUtils.get(columns, 1));
		input.setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 2)));
		Uncertainty uncertainty = Uncertainty.fromCsv(columns, 3);
		input.setUncertaintyDistribution(uncertainty);
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 7));
		input.setComment(comment);
		input.setPedigreeUncertainty(CsvUtils.getPedigreeUncertainty(comment));
		return input;
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[8];
		line[0] = getName();
		line[1] = getUnit();
		line[2] = getAmount();
		if (getUncertaintyDistribution() != null)
			getUncertaintyDistribution().toCsv(line, 3);
		else
			Uncertainty.undefinedToCsv(line, 3);
		String comment = getComment();
		if (comment == null)
			comment = "";
		String pedigree = getPedigreeUncertainty();
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[7] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(config).join(line);
	}

}
