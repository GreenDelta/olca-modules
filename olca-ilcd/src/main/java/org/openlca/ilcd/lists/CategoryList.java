package org.openlca.ilcd.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CategoriesType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CategoriesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="category" type="{http://lca.jrc.it/ILCD/Categories}CategoryType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dataType" use="required" type="{http://lca.jrc.it/ILCD/Categories}DataSetType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoriesType", propOrder = { "categories" })
public class CategoryList implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "category", required = true)
	protected List<Category> categories;
	@XmlAttribute(name = "dataType", required = true)
	protected DataSetType dataType;

	/**
	 * Gets the value of the categories property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the categories property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCategories().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Category }
	 * 
	 * 
	 */
	public List<Category> getCategories() {
		if (categories == null) {
			categories = new ArrayList<>();
		}
		return this.categories;
	}

	/**
	 * Gets the value of the dataType property.
	 * 
	 * @return possible object is {@link DataSetType }
	 * 
	 */
	public DataSetType getDataType() {
		return dataType;
	}

	/**
	 * Sets the value of the dataType property.
	 * 
	 * @param value
	 *            allowed object is {@link DataSetType }
	 * 
	 */
	public void setDataType(DataSetType value) {
		this.dataType = value;
	}

}
