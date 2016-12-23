package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Activity {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String activityNameId;

	@XmlAttribute
	public String activityNameContextId;

	@XmlAttribute
	public String parentActivityId;

	@XmlAttribute
	public String parentActivityContextId;

	@XmlAttribute
	public Integer inheritanceDepth;

	@XmlAttribute
	public int type;

	@XmlAttribute
	public int specialActivityType;

	@XmlAttribute
	public Integer energyValues;

	@XmlAttribute
	public String masterAllocationPropertyId;

	@XmlAttribute
	public String masterAllocationPropertyContextId;

	@XmlElement(name = "activityName")
	public String name;

	@XmlElement(name = "synonym")
	public final List<String> synonyms = new ArrayList<String>();

	public String includedActivitiesStart;

	public String includedActivitiesEnd;

	public RichText allocationComment;

	public RichText generalComment;

	@XmlElement(name = "tag")
	public final List<String> tags = new ArrayList<String>();

}
