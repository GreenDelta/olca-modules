package org.openlca.simapro.csv.model.process;

import org.openlca.simapro.csv.CsvConfig;
import org.openlca.simapro.csv.CsvUtils;
import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.Uncertainty;

public class ElementaryExchangeRow extends AbstractExchangeRow {

	private String subCompartment;

	public String getSubCompartment() {
		return subCompartment;
	}

	public void setSubCompartment(String subCompartment) {
		this.subCompartment = subCompartment;
	}

	@Override
	public void fill(String line, CsvConfig config) {
		String[] columns = CsvUtils.split(line, config);
		setName(CsvUtils.get(columns, 0));
		setSubCompartment(CsvUtils.get(columns, 1));
		setUnit(CsvUtils.get(columns, 2));
		setAmount(CsvUtils.formatNumber(CsvUtils.get(columns, 3)));
		Uncertainty uncertainty = Uncertainty.fromCsv(columns, 4);
		setUncertaintyDistribution(uncertainty);
		String comment = CsvUtils.readMultilines(CsvUtils.get(columns, 8));
		setComment(comment);
		setPedigreeUncertainty(CsvUtils.getPedigreeUncertainty(comment));
	}

	public String toCsv(CsvConfig config) {
		String[] line = new String[9];
		line[0] = getName();
		line[1] = subCompartment;
		line[2] = getUnit();
		line[3] = getAmount();
		if (getUncertaintyDistribution() != null)
			getUncertaintyDistribution().toCsv(line, 4);
		else
			Uncertainty.undefinedToCsv(line, 4);
		String comment = getComment();
		if (comment == null)
			comment = "";
		String pedigree = getPedigreeUncertainty();
		if (pedigree != null && !comment.contains(pedigree))
			comment = pedigree + "\n" + comment;
		line[8] = CsvUtils.writeMultilines(comment);
		return CsvUtils.getJoiner(config).join(line);
	}

	@Override
	public String toString() {
		return "ElementaryExchangeRow [subCompartment=" + subCompartment
				+ ", getName()=" + getName() + ", getUnit()=" + getUnit() + "]";
	}

}
