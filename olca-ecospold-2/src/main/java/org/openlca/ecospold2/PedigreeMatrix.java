package org.openlca.ecospold2;

import org.jdom2.Element;

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

	static PedigreeMatrix fromXml(Element e) {
		if (e == null)
			return null;
		PedigreeMatrix matrix = new PedigreeMatrix();
		matrix.completeness = In.integer(e.getAttributeValue("completeness"));
		matrix.furtherTechnologyCorrelation = In.integer(e
				.getAttributeValue("furtherTechnologyCorrelation"));
		matrix.geographicalCorrelation = In.integer(e
				.getAttributeValue("geographicalCorrelation"));
		matrix.reliability = In.integer(e.getAttributeValue("reliability"));
		matrix.temporalCorrelation = In.integer(e
				.getAttributeValue("temporalCorrelation"));
		return matrix;
	}

	Element toXml() {
		Element element = new Element("pedigreeMatrix", IO.NS);
		element.setAttribute("completeness", Integer.toString(completeness));
		element.setAttribute("furtherTechnologyCorrelation",
				Integer.toString(furtherTechnologyCorrelation));
		element.setAttribute("geographicalCorrelation",
				Integer.toString(geographicalCorrelation));
		element.setAttribute("reliability", Integer.toString(reliability));
		element.setAttribute("temporalCorrelation",
				Integer.toString(temporalCorrelation));
		return element;
	}

}
