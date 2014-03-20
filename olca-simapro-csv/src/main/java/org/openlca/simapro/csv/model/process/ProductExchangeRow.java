package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.Uncertainty;

public class ProductExchangeRow extends AbstractExchangeRow {

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		setUnit(CsvUtils.get(columns, 1));
		setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 2)));
		Uncertainty uncertainty = Uncertainty.fromCsv(columns, 3);
		setUncertaintyDistribution(uncertainty);
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 7));
		setComment(comment);
		setPedigreeUncertainty(CsvUtils.getPedigreeUncertainty(comment));
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

	@Override
	public String toString() {
		return "ProductExchangeRow [getName()=" + getName() + ", getUnit()="
				+ getUnit() + "]";
	}

}
