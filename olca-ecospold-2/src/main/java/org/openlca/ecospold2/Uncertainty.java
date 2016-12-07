package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Uncertainty {

	@XmlElement(name = "lognormal")
	public LogNormal logNormal;

	public Normal normal;

	public Triangular triangular;

	public Uniform uniform;

	public UndefinedUncertainty undefined;

	public PedigreeMatrix pedigreeMatrix;

	public String comment;

	static Uncertainty fromXml(Element element) {
		if (element == null)
			return null;
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.logNormal = LogNormal.fromXml(In
				.child(element, "lognormal"));
		uncertainty.triangular = Triangular.fromXml(In.child(element,
				"triangular"));
		uncertainty.normal = Normal.fromXml(In.child(element, "normal"));
		uncertainty.uniform = Uniform.fromXml(In.child(element, "uniform"));
		uncertainty.undefined = UndefinedUncertainty.fromXml(In.child(element,
				"undefined"));
		uncertainty.pedigreeMatrix = PedigreeMatrix.fromXml(In.child(element,
				"pedigreeMatrix"));
		uncertainty.comment = In.childText(element, "comment");
		return uncertainty;
	}

	Element toXml() {
		Element element = new Element("uncertainty", IO.NS);
		if (logNormal != null)
			element.addContent(logNormal.toXml());
		if (triangular != null)
			element.addContent(triangular.toXml());
		if (normal != null)
			element.addContent(normal.toXml());
		if (uniform != null)
			element.addContent(uniform.toXml());
		if (undefined != null)
			element.addContent(undefined.toXml());
		if (pedigreeMatrix != null)
			element.addContent(pedigreeMatrix.toXml());
		if (comment != null)
			Out.addChild(element, "comment", comment);
		return element;
	}

}
