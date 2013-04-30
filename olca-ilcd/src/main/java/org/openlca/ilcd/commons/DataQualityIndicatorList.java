package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for DataQualityIndicatorsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DataQualityIndicatorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataQualityIndicator" type="{http://lca.jrc.it/ILCD/Common}DataQualityIndicatorType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataQualityIndicatorsType", propOrder = { "indicators" })
public class DataQualityIndicatorList implements Serializable {

	private final static long serialVersionUID = 1L;
	@XmlElement(name = "dataQualityIndicator", required = true)
	protected List<DataQualityIndicator> indicators;

	/**
	 * Gets the value of the indicators property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the indicators property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getIndicators().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DataQualityIndicator }
	 * 
	 * 
	 */
	public List<DataQualityIndicator> getIndicators() {
		if (indicators == null) {
			indicators = new ArrayList<>();
		}
		return this.indicators;
	}

}
