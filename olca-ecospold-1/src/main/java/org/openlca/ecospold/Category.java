package org.openlca.ecospold;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Category {

	@XmlAttribute(name = "type")
	private int type;

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "localName")
	private String localName;

	@XmlElement(
			name = "subCategory",
			namespace = "http://www.EcoInvent.org/Categories")
	private List<SubCategory> subCategories = new ArrayList<>();

	/**
	 * The corresponding codes are: 0=Technosphere, 1=Nature, 2=Impact
	 * assessment.
	 */
	public int getType() {
		return type;
	}

	/**
	 * The corresponding codes are: 0=Technosphere, 1=Nature, 2=Impact
	 * assessment.
	 */
	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public List<SubCategory> getSubCategories() {
		return subCategories;
	}

}
