package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class UnitConversion {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public double factor;

	@XmlElement(name = "unitFromName")
	public String fromUnit;

	@XmlElement(name = "unitToName")
	public String toUnit;

	@XmlElement(name = "unitType")
	public String quantity;

}
