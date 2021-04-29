package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class PedigreeMatrix {

	@XmlAttribute
	public int reliability;

	@XmlAttribute
	public int completeness;

	@XmlAttribute
	public int temporalCorrelation;

	@XmlAttribute
	public int geographicalCorrelation;

	@XmlAttribute(name = "furtherTechnologyCorrelation")
	public int technologyCorrelation;

}
