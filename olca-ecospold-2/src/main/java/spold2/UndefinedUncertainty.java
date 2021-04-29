package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class UndefinedUncertainty {

	@XmlAttribute
	public double minValue;

	@XmlAttribute
	public double maxValue;

	@XmlAttribute
	public double standardDeviation95;

}
