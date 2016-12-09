package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Triangular {

	@XmlAttribute
	public double minValue;

	@XmlAttribute
	public double mostLikelyValue;

	@XmlAttribute
	public double maxValue;

}
