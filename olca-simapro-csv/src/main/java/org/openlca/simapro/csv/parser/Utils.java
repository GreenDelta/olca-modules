package org.openlca.simapro.csv.parser;

import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.SPLogNormalDistribution;
import org.openlca.simapro.csv.model.SPNormalDistribution;
import org.openlca.simapro.csv.model.SPPedigreeMatrix;
import org.openlca.simapro.csv.model.SPTriangleDistribution;
import org.openlca.simapro.csv.model.SPUniformDistribution;

final class Utils {

	static String formatNumber(String value) {
		if (value == null)
			return null;
		return value.replace(",", ".");
	}

	static String replaceCSVSeperator(String value, String csvSeperator) {
		if (value == null)
			return null;
		return value.replace(csvSeperator, "");
	}

	static IDistribution createDistibution(String type, String value1,
			String value2, String value3, String comment) {
		if (type == null)
			return null;
		switch (type.toLowerCase()) {
		case "lognormal":
			return new SPLogNormalDistribution(Double.parseDouble(value1),
					getPedigreeMatrix(comment));
		case "normal":
			return new SPNormalDistribution(Double.parseDouble(value1));
		case "triangle":
			return new SPTriangleDistribution(Double.parseDouble(value2),
					Double.parseDouble(value3));
		case "uniform":
			return new SPUniformDistribution(Double.parseDouble(value2),
					Double.parseDouble(value3));
		default:
			return null;
		}
	}

	private static SPPedigreeMatrix getPedigreeMatrix(String comment) {
		if (!comment.startsWith("(") && !comment.contains(")"))
			return null;
		String[] pedigree = comment.substring(1, comment.indexOf(")")).split(
				",");
		if (pedigree.length != 6)
			return null;
		SPPedigreeMatrix matrix = new SPPedigreeMatrix();
		matrix.setReliability(pedigree[0]);
		matrix.setCompleteness(pedigree[1]);
		matrix.setTemporalCorrelation(pedigree[2]);
		matrix.setGeographicalCorrelation(pedigree[3]);
		matrix.setTechnologicalCorrelation(pedigree[4]);
		matrix.setSampleSize(pedigree[5]);
		return matrix;
	}

}
