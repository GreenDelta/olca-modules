package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Representativeness {

	@XmlAttribute
	public Double percent;

	@XmlAttribute
	public String systemModelId;

	public String systemModelName;

	public String samplingProcedure;

	public String extrapolations;

}
