package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MacroEconomicScenario {

	@XmlAttribute(name = "macroEconomicScenarioId")
	public String id;

	public String name;

	public String comment;

}
