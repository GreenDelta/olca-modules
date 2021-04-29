package spold2;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

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
