package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Triangular {

	@XmlAttribute
	public double minValue;

	@XmlAttribute
	public double mostLikelyValue;

	@XmlAttribute
	public double maxValue;

}
