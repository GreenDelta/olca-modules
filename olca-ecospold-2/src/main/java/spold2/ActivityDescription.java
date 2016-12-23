package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityDescription {

	public Activity activity;

	@XmlElement(name = "classification")
	public final List<Classification> classifications = new ArrayList<>();

	public Geography geography;

	public Technology technology;

	public Time timePeriod;

	public MacroEconomicScenario macroEconomicScenario;

}
