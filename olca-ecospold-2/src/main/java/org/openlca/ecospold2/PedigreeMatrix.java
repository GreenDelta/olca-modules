package org.openlca.ecospold2;

public class PedigreeMatrix {

	private int reliability;
	private int completeness;
	private int temporalCorrelation;
	private int geographicalCorrelation;
	private int furtherTechnologyCorrelation;

	public int getReliability() {
		return reliability;
	}

	public void setReliability(int reliability) {
		this.reliability = reliability;
	}

	public int getCompleteness() {
		return completeness;
	}

	public void setCompleteness(int completeness) {
		this.completeness = completeness;
	}

	public int getTemporalCorrelation() {
		return temporalCorrelation;
	}

	public void setTemporalCorrelation(int temporalCorrelation) {
		this.temporalCorrelation = temporalCorrelation;
	}

	public int getGeographicalCorrelation() {
		return geographicalCorrelation;
	}

	public void setGeographicalCorrelation(int geographicalCorrelation) {
		this.geographicalCorrelation = geographicalCorrelation;
	}

	public int getFurtherTechnologyCorrelation() {
		return furtherTechnologyCorrelation;
	}

	public void setFurtherTechnologyCorrelation(int furtherTechnologyCorrelation) {
		this.furtherTechnologyCorrelation = furtherTechnologyCorrelation;
	}

}
