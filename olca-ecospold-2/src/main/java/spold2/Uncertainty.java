package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

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

}
