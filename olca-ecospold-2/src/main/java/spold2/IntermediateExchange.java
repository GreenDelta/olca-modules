package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"name",
		"unit",
		"comment",
		"uncertainty",
		"properties",
		"classifications",
		"inputGroup",
		"outputGroup"
})
public class IntermediateExchange extends Exchange {

	@XmlAttribute(name = "intermediateExchangeId")
	public String flowId;

	@XmlAttribute
	public String activityLinkId;

	@XmlAttribute
	public Double productionVolumeAmount;

	@XmlAttribute
	public String productionVolumeVariableName;

	@XmlAttribute
	public String productionVolumeMathematicalRelation;

	@XmlElement(name = "classification")
	public final List<Classification> classifications = new ArrayList<>();

}
