package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Normal {

	@XmlAttribute
	public double meanValue;

	@XmlAttribute
	public double variance;

	@XmlAttribute
	public double varianceWithPedigreeUncertainty;

}
