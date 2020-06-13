package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.Uncertainty;

public class ProductExchangeRow extends ExchangeRow {

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		this.unit = CsvUtils.get(columns, 1);
		this.amount = CsvUtils.formatNumber(CsvUtils.get(columns, 2));
		Uncertainty uncertainty = Uncertainty.fromCsv(columns, 3);
		this.uncertaintyDistribution = uncertainty;
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 7));
		this.comment = comment;
		this.pedigreeUncertainty = CsvUtils.getPedigreeUncertainty(comment);
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[8];
		line[0] = name;
		line[1] = unit;
		line[2] = amount;
		if (uncertaintyDistribution != null)
			uncertaintyDistribution.toCsv(line, 3);
		else
			Uncertainty.undefinedToCsv(line, 3);
		String comment = this.comment;
		if (comment == null)
			comment = "";
		String pedigree = pedigreeUncertainty;
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[7] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(config).join(line);
	}

	@Override
	public String toString() {
		return "ProductExchangeRow [getName()=" + name + ", getUnit()="
				+ unit + "]";
	}

}
