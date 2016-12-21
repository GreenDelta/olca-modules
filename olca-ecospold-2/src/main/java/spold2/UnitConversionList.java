package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "validUnitConversions")
public class UnitConversionList {

	@XmlElement(name = "unitConversion")
	public final List<UnitConversion> conversions = new ArrayList<>();

}
