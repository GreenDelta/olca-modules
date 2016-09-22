package org.openlca.ecospold2;

import org.jdom2.Element;

public class Uncertainty {

	private LogNormal logNormal;
	private Normal normal;
	private Triangular triangular;
	private UndefinedUncertainty undefined;
	private Uniform uniform;
	public PedigreeMatrix pedigreeMatrix;
	private String comment;

	public LogNormal getLogNormal() {
		return logNormal;
	}

	public void setLogNormal(LogNormal logNormal) {
		this.logNormal = logNormal;
	}

	public Triangular getTriangular() {
		return triangular;
	}

	public void setTriangular(Triangular triangular) {
		this.triangular = triangular;
	}

	public Normal getNormal() {
		return normal;
	}

	public void setNormal(Normal normal) {
		this.normal = normal;
	}

	public Uniform getUniform() {
		return uniform;
	}

	public void setUniform(Uniform uniform) {
		this.uniform = uniform;
	}

	public UndefinedUncertainty getUndefined() {
		return undefined;
	}

	public void setUndefined(UndefinedUncertainty undefined) {
		this.undefined = undefined;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

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
