package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.Uncertainty;

public class ElementaryExchangeRow extends ExchangeRow {

	public String subCompartment;

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		this.name = CsvUtils.get(columns, 0);
		this.subCompartment = CsvUtils.get(columns, 1);
		this.unit = CsvUtils.get(columns, 2);
		this.amount = CsvUtils.formatNumber(CsvUtils.get(columns, 3));
		Uncertainty uncertainty = Uncertainty.fromCsv(columns, 4);
		this.uncertaintyDistribution = uncertainty;
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 8));
		this.comment = comment;
		this.pedigreeUncertainty = CsvUtils.getPedigreeUncertainty(comment);
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[9];
		line[0] = name;
		line[1] = subCompartment;
		line[2] = unit;
		line[3] = amount;
		if (uncertaintyDistribution != null)
			uncertaintyDistribution.toCsv(line, 4);
		else
			Uncertainty.undefinedToCsv(line, 4);
		String comment = this.comment;
		if (comment == null)
			comment = "";
		String pedigree = pedigreeUncertainty;
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[8] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(config).join(line);
	}

	@Override
	public String toString() {
		return "ElementaryExchangeRow [subCompartment=" + subCompartment
				+ ", getName()=" + name + ", getUnit()=" + unit + "]";
	}

}
