package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

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
