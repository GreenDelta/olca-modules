package org.openlca.simapro.csv.model;

import java.util.HashSet;
import java.util.Set;

import org.openlca.simapro.csv.model.enums.pedigree.Completeness;
import org.openlca.simapro.csv.model.enums.pedigree.GeographicalCorrelation;
import org.openlca.simapro.csv.model.enums.pedigree.Reliability;
import org.openlca.simapro.csv.model.enums.pedigree.SampleSize;
import org.openlca.simapro.csv.model.enums.pedigree.TechnologicalCorrelation;
import org.openlca.simapro.csv.model.enums.pedigree.TemporalCorrelation;

public class SPPedigreeMatrix {

	private static Set<String> pedigreeContain = new HashSet<String>();

	public Reliability reliability = Reliability.NA;
	public Completeness completeness = Completeness.NA;
	public TemporalCorrelation temporalCorrelation = TemporalCorrelation.NA;
	public GeographicalCorrelation geographicalCorrelation = GeographicalCorrelation.NA;
	public TechnologicalCorrelation technologicalCorrelation = TechnologicalCorrelation.NA;
	public SampleSize sampleSize = SampleSize.NA;

	public SPPedigreeMatrix() {
		pedigreeContain.add("1");
		pedigreeContain.add("2");
		pedigreeContain.add("3");
		pedigreeContain.add("4");
		pedigreeContain.add("5");
	}

	public String getPedigreeCommentString() {
		String matrix = null;
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(reliability.getKey());
		builder.append(",");
		builder.append(completeness.getKey());
		builder.append(",");
		builder.append(temporalCorrelation.getKey());
		builder.append(",");
		builder.append(geographicalCorrelation.getKey());
		builder.append(",");
		builder.append(technologicalCorrelation.getKey());
		builder.append(",");
		builder.append(sampleSize.getKey());
		builder.append(")");

		matrix = builder.toString();

		return matrix;
	}

	public double getUncertainty() {
		double u1 = Math.pow(Math.log(reliability.getIndicator()), 2);
		double u2 = Math.pow(Math.log(completeness.getIndicator()), 2);
		double u3 = Math.pow(Math.log(temporalCorrelation.getIndicator()), 2);
		double u4 = Math.pow(Math.log(geographicalCorrelation.getIndicator()),
				2);
		double u5 = Math.pow(
				Math.log(technologicalCorrelation.getIndicator()), 2);
		double u6 = Math.pow(Math.log(sampleSize.getIndicator()), 2);
		double ub = Math.pow(Math.log(1.05), 2);

		return Math.exp(Math.sqrt(u1 + u2 + u3 + u4 + u5 + u6 + ub));
	}

	public void setCompleteness(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				completeness = Completeness.ONE;
				break;

			case 2:
				completeness = Completeness.TWO;
				break;

			case 3:
				completeness = Completeness.THREE;
				break;

			case 4:
				completeness = Completeness.FOUR;
				break;

			case 5:
				completeness = Completeness.FIVE;
				break;
			}
		} else {
			completeness = Completeness.NA;
		}
	}

	public void setTechnologicalCorrelation(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				technologicalCorrelation = TechnologicalCorrelation.ONE;
				break;

			case 2:
				technologicalCorrelation = TechnologicalCorrelation.TWO;
				break;

			case 3:
				technologicalCorrelation = TechnologicalCorrelation.THREE;
				break;

			case 4:
				technologicalCorrelation = TechnologicalCorrelation.FOUR;
				break;

			case 5:
				technologicalCorrelation = TechnologicalCorrelation.FIVE;
				break;
			}
		} else {
			technologicalCorrelation = TechnologicalCorrelation.NA;
		}
	}

	public void setGeographicalCorrelation(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				geographicalCorrelation = GeographicalCorrelation.ONE;
				break;

			case 2:
				geographicalCorrelation = GeographicalCorrelation.TWO;
				break;

			case 3:
				geographicalCorrelation = GeographicalCorrelation.THREE;
				break;

			case 4:
				geographicalCorrelation = GeographicalCorrelation.FOUR;
				break;

			case 5:
				geographicalCorrelation = GeographicalCorrelation.FIVE;
				break;
			}
		} else {
			geographicalCorrelation = GeographicalCorrelation.NA;
		}
	}

	public void setReliability(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				reliability = Reliability.ONE;
				break;

			case 2:
				reliability = Reliability.TWO;
				break;

			case 3:
				reliability = Reliability.THREE;
				break;

			case 4:
				reliability = Reliability.FOUR;
				break;

			case 5:
				reliability = Reliability.FIVE;
				break;
			}
		} else {
			reliability = Reliability.NA;
		}
	}

	public void setSampleSize(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				sampleSize = SampleSize.ONE;
				break;

			case 2:
				sampleSize = SampleSize.TWO;
				break;

			case 3:
				sampleSize = SampleSize.THREE;
				break;

			case 4:
				sampleSize = SampleSize.FOUR;
				break;

			case 5:
				sampleSize = SampleSize.FIVE;
				break;
			}
		} else {
			sampleSize = SampleSize.NA;
		}
	}

	public void setTemporalCorrelation(String pedigree) {
		if (pedigreeContain.contains(pedigree)) {
			switch (Integer.parseInt(pedigree)) {
			case 1:
				temporalCorrelation = TemporalCorrelation.ONE;
				break;

			case 2:
				temporalCorrelation = TemporalCorrelation.TWO;
				break;

			case 3:
				temporalCorrelation = TemporalCorrelation.THREE;
				break;

			case 4:
				temporalCorrelation = TemporalCorrelation.FOUR;
				break;

			case 5:
				temporalCorrelation = TemporalCorrelation.FIVE;
				break;
			}
		} else {
			temporalCorrelation = TemporalCorrelation.NA;
		}

	}

}
