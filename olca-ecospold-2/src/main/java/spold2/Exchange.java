package spold2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class Exchange {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String unitId;

	@XmlAttribute
	public String variableName;

	@XmlAttribute
	public String casNumber;

	/**
	 * Amount is a reference type because it is optional as exchanges are also
	 * used in the master data
	 */
	@XmlAttribute
	public Double amount;

	@XmlAttribute(name = "isCalculatedAmount")
	public Boolean isCalculated;

	@XmlAttribute
	public String mathematicalRelation;

	public String name;

	@XmlElement(name = "unitName")
	public String unit;

	public String comment;

	public Uncertainty uncertainty;

	public Integer outputGroup;

	public Integer inputGroup;

	@XmlElement(name = "property")
	public final List<Property> properties = new ArrayList<>();

}
