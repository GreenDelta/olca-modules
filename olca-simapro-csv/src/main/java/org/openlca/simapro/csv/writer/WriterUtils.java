package org.openlca.simapro.csv.writer;

import java.io.IOException;

import org.openlca.simapro.csv.model.IDistribution;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.SPLogNormalDistribution;
import org.openlca.simapro.csv.model.enums.DistributionParameter;
import org.openlca.simapro.csv.model.enums.DistributionType;

final class WriterUtils {

	/**
	 * Fixes the line ends in multi-line comments: line ends in comments are
	 * indicated by by the ASCII 127 sign (delete character). Additionally, the
	 * comment must end with this delete character. The comment is set in
	 * quotation marks and quotation marks within the comment are replaced by
	 * double quotation marks.
	 * */
	static String comment(String rawComment) {
		if (rawComment == null)
			return null;
		char char127 = 127;
		String comment = rawComment.replaceAll("\"", "\"\"");
		comment = comment.replaceAll("\\r?\\n", "" + char127);
		return "\"" + comment + char127 + "\"";
	}

	static String getInputParameterLine(InputParameterRow parameter,
			char csvSeperator, char decimalSeperator) throws IOException {
		String line = parameter.getName()
				+ csvSeperator
				+ Double.toString(parameter.getValue()).replace(".",
						String.valueOf(decimalSeperator)) + csvSeperator;
		line += getDistributionPart(parameter.getUncertainty(), csvSeperator,
				decimalSeperator);
		line += (parameter.isHidden() ? "Yes" : "No") + csvSeperator;
		if (parameter.getComment() != null) {
			line += comment(parameter.getComment());
		}
		return line;
	}

	static String getCalculatedParameterLine(CalculatedParameterRow parameter,
			char csvSeperator, char decimalSeperator) throws IOException {
		String line = parameter.getName()
				+ csvSeperator
				+ parameter.getExpression().replace(".",
						String.valueOf(decimalSeperator)) + csvSeperator;
		if (parameter.getComment() != null)
			line += comment(parameter.getComment());
		return line;
	}

	static String getDistributionPart(IDistribution distribution,
			char csvSeperator, char decimalSeperator) {
		String line = "";
		if (distribution == null) {
			line = "Undefined" + csvSeperator + '0' + csvSeperator + '0'
					+ csvSeperator + '0' + csvSeperator;
		} else {
			DistributionType type = distribution.getType();
			if (type == null) {
				type = DistributionType.UNDEFINED;
			}
			line = type.getValue().replace('.', decimalSeperator)
					+ csvSeperator;
			switch (distribution.getType()) {
			case LOG_NORMAL:
				SPLogNormalDistribution logNormalDistribution = (SPLogNormalDistribution) distribution;
				line += String
						.valueOf(distribution
								.getDistributionParameter(DistributionParameter.SQUARED_STANDARD_DEVIATION))
						+ csvSeperator
						+ '0'
						+ csvSeperator
						+ '0'
						+ csvSeperator;
				if (logNormalDistribution.getPedigreeMatrix() != null)
					line += logNormalDistribution.getPedigreeMatrix()
							.getPedigreeCommentString();
				break;
			case NORMAL:
				line += String
						.valueOf(distribution
								.getDistributionParameter(DistributionParameter.DOUBLED_STANDARD_DEVIATION))
						+ csvSeperator
						+ '0'
						+ csvSeperator
						+ '0'
						+ csvSeperator;
				break;
			case TRIANGLE:
				line += '0'
						+ csvSeperator
						+ String.valueOf(distribution
								.getDistributionParameter(DistributionParameter.MINIMUM))
						+ csvSeperator
						+ String.valueOf(distribution
								.getDistributionParameter(DistributionParameter.MAXIMUM))
						+ csvSeperator;
				break;
			case UNIFORM:
				line += '0'
						+ csvSeperator
						+ String.valueOf(distribution
								.getDistributionParameter(DistributionParameter.MINIMUM))
						+ csvSeperator
						+ String.valueOf(distribution
								.getDistributionParameter(DistributionParameter.MAXIMUM))
						+ csvSeperator;
				break;
			case UNDEFINED:
				line += +csvSeperator + '0' + csvSeperator + '0' + csvSeperator
						+ '0' + csvSeperator;
				break;
			}
		}
		return line;
	}
}
