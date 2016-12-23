package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

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
