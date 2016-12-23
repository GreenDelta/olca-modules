package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class LogNormal {

	@XmlAttribute
	public double meanValue;

	@XmlAttribute
	public double mu;

	@XmlAttribute
	public double variance;

	@XmlAttribute
	public double varianceWithPedigreeUncertainty;

}
