package org.openlca.ecospold2;

import org.jdom2.Element;

public class PedigreeMatrix {

	public int reliability;
	public int completeness;
	public int temporalCorrelation;
	public int geographicalCorrelation;
	public int technologyCorrelation;

	static PedigreeMatrix fromXml(Element e) {
		if (e == null)
			return null;
		PedigreeMatrix matrix = new PedigreeMatrix();
		matrix.completeness = In.integer(e.getAttributeValue("completeness"));
		matrix.technologyCorrelation = In.integer(e
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
				Integer.toString(technologyCorrelation));
		element.setAttribute("geographicalCorrelation",
				Integer.toString(geographicalCorrelation));
		element.setAttribute("reliability", Integer.toString(reliability));
		element.setAttribute("temporalCorrelation",
				Integer.toString(temporalCorrelation));
		return element;
	}

}
