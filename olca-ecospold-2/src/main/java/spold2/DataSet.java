package spold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataSet {

	@XmlElement(name = "activityDescription")
	public ActivityDescription description;

	@XmlElement(name = "flowData")
	public FlowData flowData;

	@XmlElement(name = "modellingAndValidation")
	public Validation validation;

	@XmlElement(name = "administrativeInformation")
	public AdminInfo adminInfo;

	@XmlElement(name = "usedUserMasterData", namespace = "http://www.EcoInvent.org/UsedUserMasterData")
	public UserMasterData masterData;

}
