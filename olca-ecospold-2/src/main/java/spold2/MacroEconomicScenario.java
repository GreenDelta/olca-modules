package spold2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MacroEconomicScenario {

	@XmlAttribute(name = "macroEconomicScenarioId")
	public String id;

	public String name;

	public String comment;

}
