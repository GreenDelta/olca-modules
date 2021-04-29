package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

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
