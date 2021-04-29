package spold2;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "validUnitConversions")
public class UnitConversionList {

	@XmlElement(name = "unitConversion")
	public final List<UnitConversion> conversions = new ArrayList<>();

}
