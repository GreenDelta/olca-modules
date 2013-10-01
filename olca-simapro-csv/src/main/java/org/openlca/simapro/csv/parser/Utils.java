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
		IDistribution distribution = null;
		switch (type.toLowerCase()) {
		case "lognormal":
			distribution = new SPLogNormalDistribution(
					Double.parseDouble(value1), getPedigreeMatrix(comment));
			break;
		case "normal":
			distribution = new SPNormalDistribution(Double.parseDouble(value1));
			break;
		case "triangle":
			distribution = new SPTriangleDistribution(
					Double.parseDouble(value2), Double.parseDouble(value3));
			break;
		case "uniform":
			distribution = new SPUniformDistribution(
					Double.parseDouble(value2), Double.parseDouble(value3));
		}
		return distribution;
	}

	private static SPPedigreeMatrix getPedigreeMatrix(String comment) {
		SPPedigreeMatrix matrix = null;
		if (comment.startsWith("(")) {

			String[] pedigree = comment.substring(1, comment.indexOf(")"))
					.split(",");
			if (pedigree.length == 6) {
				matrix = new SPPedigreeMatrix();
				matrix.setReliability(pedigree[0]);
				matrix.setCompleteness(pedigree[1]);
				matrix.setTemporalCorrelation(pedigree[2]);
				matrix.setGeographicalCorrelation(pedigree[3]);
				matrix.setFurtherTechnologicalCorrelation(pedigree[4]);
				matrix.setSampleSize(pedigree[5]);
			}
		} else {
			// TODO throw exception but at first I have to find out when it must
			// exist a matrix
		}
		return matrix;
	}

}
